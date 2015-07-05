/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.internal.proxy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import com.sonatype.nexus.repository.nuget.internal.FeedResult;
import com.sonatype.nexus.repository.nuget.internal.NugetGalleryFacet;
import com.sonatype.nexus.repository.nuget.internal.NugetGalleryFacetImpl;
import com.sonatype.nexus.repository.nuget.odata.ODataConsumer;
import com.sonatype.nexus.repository.nuget.odata.ODataUtils;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;
import org.sonatype.nexus.transaction.Transactional;
import org.sonatype.sisu.goodies.common.Time;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.http.client.utils.URIBuilder;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.min;

/**
 * A group-aware proxying {@link NugetGalleryFacet}.
 *
 * @since 3.0
 */
@Named("proxy")
public class NugetProxyGalleryFacet
    extends NugetGalleryFacetImpl
{
  private static final int TWO_PAGES = 2 * ODataUtils.PAGE_SIZE;

  private final NugetFeedFetcher fetcher;

  @VisibleForTesting
  static final String CONFIG_KEY = "nugetProxy";

  @VisibleForTesting
  static class Config
  {
    // By default, the query cache will hold up to 300 entries for an hour each
    public static final int DEFAULT_QUERY_CACHE_SIZE = 300;

    public static final int DEFAULT_QUERY_CACHE_ITEM_AGE = Time.minutes(60).toSecondsI();

    public int queryCacheSize = DEFAULT_QUERY_CACHE_SIZE;

    /**
     * Query cache item-max-age seconds.
     */
    public int queryCacheItemMaxAge = DEFAULT_QUERY_CACHE_ITEM_AGE;

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "queryCacheSize=" + queryCacheSize +
          ", queryCacheItemMaxAge=" + queryCacheItemMaxAge +
          '}';
    }
  }

  private Config config;

  private Cache<QueryCacheKey, Integer> cache;

  @Inject
  public NugetProxyGalleryFacet(final NugetFeedFetcher fetcher) {
    this.fetcher = checkNotNull(fetcher);
  }

  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    facet(ConfigurationFacet.class).validateSection(configuration, CONFIG_KEY, Config.class);
  }

  @Override
  protected void doConfigure(final Configuration configuration) throws Exception {
    config = facet(ConfigurationFacet.class).readSection(configuration, CONFIG_KEY, Config.class);
    log.debug("Config: {}", config);

    cache = CacheBuilder.newBuilder()
        .maximumSize(config.queryCacheSize)
        .expireAfterWrite(config.queryCacheItemMaxAge, TimeUnit.SECONDS)
        .build();
  }

  @Override
  protected void doDestroy() throws Exception {
    cache = null;
    config = null;
  }

  @Override
  public int count(final String operation, final Map<String, String> parameters) {
    final List<Integer> remoteCounts = passQueryToRemoteRepos(nugetQuery(operation, parameters),
        getProxyRepositories(),
        new CountFetcher(fetcher));

    final int hostedCount = count(operation, parameters, getHostedRepositories());

    return sum(remoteCounts) + hostedCount;
  }

  @Override
  public String feed(final String base, final String operation, final Map<String, String> query) {
    final Integer top = asInteger(query.get("$top"));
    final Integer skip = asInteger(query.get("$skip"));
    Map<String, String> remoteQuery = modifyQueryForRemote(operation, query, top, skip);

    // Populate local metadata with the remote metadata
    final List<Integer> remoteCounts = passQueryToRemoteRepos(nugetQuery(operation, remoteQuery),
        getProxyRepositories(),
        new FeedLoader(fetcher));

    // Now re-run the query locally
    final FeedResult localResult = feed(base, operation, query, getRepositories());

    // Work out the number of results we should report to the client.
    // Note that nuget.org itself occasionally reports nonsensical results.
    final int localCount = firstNonNull(localResult.getCount(), 0);
    int reportedCount = CountReportingPolicy.determineReportedCount(remoteCounts, localCount, top, skip);
    if ("Search".equals(operation)) {
      // If we're searching, cap results at 40 like nuget.org.
      reportedCount = min(ODataUtils.PAGE_SIZE, reportedCount);
    }

    localResult.setCount(reportedCount);
    return renderFeedResults(localResult);
  }

  private Map<String, String> modifyQueryForRemote(final String operation,
                                                   final Map<String, String> query,
                                                   final Integer top,
                                                   final Integer skip)
  {
    Map<String, String> remoteQuery = Maps.newHashMap(query);
    if ("Search".equals(operation) && top != null && skip != null) {
      // If we're searching, then instead of passing on the request for whatever page of results the client is looking
      // for, just get the first two pages of results right away. This makes sure our metadata is sufficiently full of
      // results before we query it, which is important since we use a different search algorithm than nuget.org.
      remoteQuery.put("$top", "" + TWO_PAGES);
      remoteQuery.put("$skip", "0");
    }
    else if (top == null || top > TWO_PAGES) {
      // limit any unbounded queries to twice our local page size
      // limit any excessive queries to twice our local page size
      remoteQuery.put("$top", "" + TWO_PAGES);
    }
    return remoteQuery;
  }

  @Override
  @Transactional
  public String entry(final String base, final String id, final String version) {
    String entryXml = super.entry(base, id, version);

    if (entryXml == null) {
      final String remoteQuery = "Packages(Id='" + id + "',Version='" + version + "')";

      passQueryToRemoteRepos(nugetQuery(remoteQuery), getProxyRepositories(), new FeedLoader(fetcher));
      entryXml = super.entry(base, id, version);
    }
    return entryXml;
  }

  private URI nugetQuery(final String path) {
    return nugetQuery(path, Collections.<String, String>emptyMap());
  }

  /**
   * Returns a relative URI for the NuGet query, properly encoding the query parameters.
   * e.g. {@code "Search()/$count?filter=whatever"}
   */
  private URI nugetQuery(final String path, final Map<String, String> queryParams) {
    try {
      URIBuilder uri = new URIBuilder();

      uri.setPath(path);

      if (!queryParams.isEmpty()) {
        List<String> paramClauses = Lists.newArrayList();
        for (Entry<String, String> param : queryParams.entrySet()) {
          paramClauses.add(param.getKey() + "=" + param.getValue());
        }
        uri.setCustomQuery(Joiner.on('&').join(paramClauses));
      }

      return uri.build();
    }
    catch (URISyntaxException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Determines which of the repository IDs correspond to remote proxies, and queries (or populates) the count cache
   * using the supplied {@link RemoteCallFactory}.
   *
   * @return successfully returned counts, which might have fewer entries than repositories.size (or none at all)
   */
  private List<Integer> passQueryToRemoteRepos(final URI path, final Iterable<Repository> repositories,
                                               final RemoteCallFactory remoteCall)
  {
    final List<Integer> counts = new ArrayList<>();

    for (Repository repo : repositories) {
      try {
        // TODO: Determine if we should talk to the remote based on its status

        final QueryCacheKey key = new QueryCacheKey(repo.getName(), path);
        final Integer cachedCount = cache.get(key, remoteCall.build(repo, path));
        counts.add(cachedCount);
      }
      catch (ExecutionException | UncheckedExecutionException e) {
        log.warn("Exception attempting to contact proxied repository {}.", repo.getName(), e.getCause());
      }
      catch (Exception e) {
        log.warn("Exception attempting to contact proxied repository {}.", repo.getName(), e);
      }
    }
    return counts;
  }

  @Nullable
  private Integer asInteger(String value) {
    if (value == null) {
      return null;
    }
    try {
      return Integer.parseInt(value);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  private int sum(Iterable<Integer> values) {
    int total = 0;
    for (final Integer i : values) {
      total += checkNotNull(i);
    }
    return total;
  }

  /**
   * A factory to create {@link Callable}s to populate the count cache.
   */
  private abstract static class RemoteCallFactory
  {
    protected final NugetFeedFetcher fetcher;

    protected RemoteCallFactory(final NugetFeedFetcher fetcher) {
      this.fetcher = fetcher;
    }

    /**
     * Create a value loader to populate the query/count cache in the case where there's no value currently cached.
     */
    public abstract Callable<Integer> build(final Repository remote, final URI path);
  }

  /**
   * Queries the remote repository for feed information, storing entries in the supplied nuget gallery.
   */
  private static class FeedLoader
      extends RemoteCallFactory
  {
    private FeedLoader(final NugetFeedFetcher fetcher) {
      super(fetcher);
    }

    public Callable<Integer> build(final Repository remote, final URI nugetQuery)
    {
      final NugetGalleryFacet gallery = remote.facet(NugetGalleryFacet.class);
      return new Callable<Integer>()
      {
        @Override
        public Integer call() throws Exception {
          // The count cache shouldn't contain nulls, so return 0 if we fail to get a result from the feed
          return firstNonNull(fetcher.cachePackageFeed(remote, nugetQuery, false, new ODataConsumer()
          {
            @Override
            public void consume(final Map<String, String> data) {
              gallery.putMetadata(data);
            }
          }), 0);
        }
      };
    }
  }

  /**
   * Queries remote repositories for a simple count of entries.
   */
  private static class CountFetcher
      extends RemoteCallFactory
  {
    private CountFetcher(final NugetFeedFetcher fetcher) {
      super(fetcher);
    }

    public Callable<Integer> build(final Repository remote, final URI nugetQuery)
    {
      return new Callable<Integer>()
      {
        @Override
        public Integer call() throws Exception {
          return firstNonNull(fetcher.getCount(remote, nugetQuery), 0);
        }
      };
    }
  }

  private static class QueryCacheKey
  {
    final String repoId;

    final URI path;

    private QueryCacheKey(final String repoId, final URI path) {
      this.repoId = repoId;
      this.path = path;
    }

    @Override
    public boolean equals(final Object o) {

      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      QueryCacheKey that = (QueryCacheKey) o;

      if (!equal(repoId, that.repoId)) {
        return false;
      }
      if (!equal(path, that.path)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return Objects.hash(path, repoId);
    }
  }
}
