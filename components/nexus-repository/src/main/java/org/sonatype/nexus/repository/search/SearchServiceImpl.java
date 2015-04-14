/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.repository.search;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.MissingFacetException;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.security.BreadActions;
import org.sonatype.nexus.repository.security.RepositoryViewPermission;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.security.SecurityHelper;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.internal.InternalSearchResponse;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_REPOSITORY_NAME;

/**
 * Default {@link SearchService} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
public class SearchServiceImpl
    extends ComponentSupport
    implements SearchService
{
  public static final String TYPE = "component";

  /**
   * Resource name of ElasticSearch mapping configuration.
   */
  public static final String MAPPING_JSON = "elasticsearch-mapping.json";

  private final Provider<Client> client;

  private final RepositoryManager repositoryManager;

  private final SecurityHelper securityHelper;

  private final List<IndexSettingsContributor> indexSettingsContributors;

  private final Map<String, ComponentMetadataProducer> componentMetadataProducers;

  @Inject
  public SearchServiceImpl(final Provider<Client> client,
                           final RepositoryManager repositoryManager,
                           final SecurityHelper securityHelper,
                           final List<IndexSettingsContributor> indexSettingsContributors,
                           final Map<String, ComponentMetadataProducer> componentMetadataProducers)
  {
    this.client = checkNotNull(client);
    this.repositoryManager = checkNotNull(repositoryManager);
    this.securityHelper = checkNotNull(securityHelper);
    this.indexSettingsContributors = checkNotNull(indexSettingsContributors);
    this.componentMetadataProducers = checkNotNull(componentMetadataProducers);
  }

  @Override
  public void createIndex(final Repository repository) {
    checkNotNull(repository);
    // TODO we should calculate the checksum of index settings and compare it with a value stored in index _meta tags
    // in case that they not match (settings changed) we should drop the index, recreate it and re-index all components
    if (!client.get().admin().indices().prepareExists(repository.getName()).execute().actionGet().isExists()) {
      // determine list of mapping configuration urls
      List<URL> urls = Lists.newArrayListWithExpectedSize(indexSettingsContributors.size() + 1);
      urls.add(Resources.getResource(getClass(), MAPPING_JSON)); // core mapping
      for (IndexSettingsContributor contributor : indexSettingsContributors) {
        URL url = contributor.getIndexSettings(repository);
        if (url != null) {
          urls.add(url);
        }
      }

      try {
        // merge all mapping configuration
        String source = "{}";
        for (URL url : urls) {
          log.debug("Merging ElasticSearch mapping: {}", url);
          String contributed = Resources.toString(url, Charsets.UTF_8);
          log.trace("Contributed ElasticSearch mapping: {}", contributed);
          source = JsonUtils.merge(source, contributed);
        }
        // update runtime configuration
        log.trace("ElasticSearch mapping: {}", source);
        log.debug("Creating index for {}", repository);
        client.get().admin().indices().prepareCreate(repository.getName())
            .setSource(source)
            .execute()
            .actionGet();
      }
      catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  @Override
  public void deleteIndex(final Repository repository) {
    checkNotNull(repository);
    if (client.get().admin().indices().prepareExists(repository.getName()).execute().actionGet().isExists()) {
      log.debug("Removing index of {}", repository);
      client.get().admin().indices().prepareDelete(repository.getName()).execute().actionGet();
    }
  }

  @Override
  public void put(final Repository repository, final Component component) {
    checkNotNull(repository);
    checkNotNull(component);
    log.debug("Indexing metadata of {} from {}", component, repository);
    try {
      Map<String, Object> additional = Maps.newHashMap();
      additional.put(P_REPOSITORY_NAME, repository.getName());
      List<Asset> assets;
      try (StorageTx tx = repository.facet(StorageFacet.class).openTx()) {
        assets = Lists.newArrayList(tx.browseAssets(component));
      }
      String json = JsonUtils.merge(componentMetadata(component, assets), JsonUtils.from(additional));
      client.get().prepareIndex(repository.getName(), TYPE, component.getEntityMetadata().getId().toString())
          .setSource(json).execute();
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void delete(final Repository repository, final Component component) {
    checkNotNull(repository);
    checkNotNull(component);
    log.debug("Removing indexed metadata of {} from {}", component, repository);
    client.get().prepareDelete(repository.getName(), TYPE, component.getEntityMetadata().getId().toString()).execute();
  }

  @Override
  public Iterable<SearchHit> browse(final QueryBuilder query) {
    checkNotNull(query);
    try {
      if (!client.get().admin().indices().prepareValidateQuery().setQuery(query).execute().actionGet().isValid()) {
        throw new IllegalArgumentException("Invalid query");
      }
    }
    catch (IndexMissingException e) {
      // no repositories were created yet, so there is no point in searching
      return null;
    }
    final String[] searchableIndexes = getSearchableIndexes();
    if (searchableIndexes.length == 0) {
      return Collections.emptyList();
    }
    return new Iterable<SearchHit>()
    {
      @Override
      public Iterator<SearchHit> iterator() {
        return new Iterator<SearchHit>()
        {
          private SearchResponse response;

          private Iterator<SearchHit> iterator;

          private boolean noMoreHits = false;

          @Override
          public boolean hasNext() {
            if (noMoreHits) {
              return false;
            }
            if (response == null) {
              response = client.get().prepareSearch(searchableIndexes)
                  .setTypes(TYPE)
                  .setQuery(query)
                  .setScroll(new TimeValue(1, TimeUnit.MINUTES))
                  .setSize(100)
                  .execute()
                  .actionGet();
              iterator = Arrays.asList(response.getHits().getHits()).iterator();
              noMoreHits = !iterator.hasNext();
            }
            else if (!iterator.hasNext()) {
              response = client.get().prepareSearchScroll(response.getScrollId())
                  .setScroll(new TimeValue(1, TimeUnit.MINUTES))
                  .execute()
                  .actionGet();
              iterator = Arrays.asList(response.getHits().getHits()).iterator();
              noMoreHits = !iterator.hasNext();
            }
            return iterator.hasNext();
          }

          @Override
          public SearchHit next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }
            return iterator.next();
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  @Override
  public Iterable<SearchHit> browse(final QueryBuilder query, final int from, final int size) {
    SearchResponse response = search(query, from, size);
    return Arrays.asList(response.getHits().getHits());
  }

  @Override
  public SearchResponse search(final QueryBuilder query, final int from, final int size) {
    checkNotNull(query);
    try {
      if (!client.get().admin().indices().prepareValidateQuery().setQuery(query).execute().actionGet().isValid()) {
        throw new IllegalArgumentException("Invalid query");
      }
    }
    catch (IndexMissingException e) {
      // no repositories were created yet, so there is no point in searching
      return new SearchResponse(InternalSearchResponse.empty(), null, 0, 0, 0, new ShardSearchFailure[]{});
    }
    final String[] searchableIndexes = getSearchableIndexes();
    if (searchableIndexes.length == 0) {
      return new SearchResponse(InternalSearchResponse.empty(), null, 0, 0, 0, new ShardSearchFailure[]{});
    }
    return client.get().prepareSearch(searchableIndexes)
        .setTypes(TYPE)
        .setQuery(query)
        .setFrom(from)
        .setSize(size)
        .execute()
        .actionGet();
  }

  private String[] getSearchableIndexes() {
    List<String> indexes = Lists.newArrayList();
    for (Repository repository : repositoryManager.browse()) {
      try {
        // check if search facet is available so avoid searching repositories without an index
        repository.facet(SearchFacet.class);
        if (repository.facet(ViewFacet.class).isOnline()
            && securityHelper.allPermitted(new RepositoryViewPermission(repository, BreadActions.BROWSE))) {
          indexes.add(repository.getName());
        }
      }
      catch (MissingFacetException e) {
        // no search facet, no search
      }
    }
    return indexes.toArray(new String[indexes.size()]);
  }

  /**
   * Creates component metadata to be indexed out of a component using {@link ComponentMetadataProducer} specific to
   * component {@link Format}.
   * If one is not available will use a default one ({@link DefaultComponentMetadataProducer}).
   */
  private String componentMetadata(final Component component, final Iterable<Asset> assets) {
    checkNotNull(component);
    String format = component.format();
    ComponentMetadataProducer producer = componentMetadataProducers.get(format);
    if (producer == null) {
      producer = componentMetadataProducers.get("default");
    }
    checkState(producer != null, "Could not find a component metadata producer for format: {}", format);
    return producer.getMetadata(component, assets);
  }

}
