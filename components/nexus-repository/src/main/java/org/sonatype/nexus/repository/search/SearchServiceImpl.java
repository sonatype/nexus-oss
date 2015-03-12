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

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.security.BreadActions;
import org.sonatype.nexus.repository.security.RepositoryViewPermission;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.security.SecurityHelper;

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

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_REPOSITORY_NAME;

/**
 * Default {@link SearchService} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
public class SearchServiceImpl
    implements SearchService
{

  public static final String TYPE = "component";

  private final Provider<Client> client;

  private final RepositoryManager repositoryManager;

  private final SecurityHelper securityHelper;

  private final List<IndexSettingsContributor> indexSettingsContributors;

  @Inject
  public SearchServiceImpl(final Provider<Client> client,
                           final RepositoryManager repositoryManager,
                           final SecurityHelper securityHelper,
                           final List<IndexSettingsContributor> indexSettingsContributors)
  {
    this.client = checkNotNull(client);
    this.repositoryManager = checkNotNull(repositoryManager);
    this.securityHelper = checkNotNull(securityHelper);
    this.indexSettingsContributors = checkNotNull(indexSettingsContributors);
  }

  @Override
  public void createIndex(final Repository repository) {
    checkNotNull(repository);
    // TODO we should calculate the checksum of index settings and compare it with a value stored in index _meta tags
    // in case that they not match (settings changed) we should drop the index, recreate it and re-index all components
    if (!client.get().admin().indices().prepareExists(repository.getName()).execute().actionGet().isExists()) {
      try {
        String source = Resources.toString(Resources.getResource(getClass(), "es-mapping.json"), UTF_8);
        for (IndexSettingsContributor contributor : indexSettingsContributors) {
          String contributed = contributor.getIndexSettings(repository);
          if (contributed != null) {
            source = JsonUtils.merge(source, contributed);
          }
        }
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
      client.get().admin().indices().prepareDelete(repository.getName()).execute().actionGet();
    }
  }

  @Override
  public void put(final Repository repository, final ComponentMetadata componentMetadata) {
    checkNotNull(repository);
    checkNotNull(componentMetadata);
    try {
      Map<String, Object> additional = Maps.newHashMap();
      additional.put(P_REPOSITORY_NAME, repository.getName());
      String json = JsonUtils.merge(componentMetadata.toJson(), JsonUtils.from(additional));
      client.get().prepareIndex(repository.getName(), TYPE, componentMetadata.getId()).setSource(json).execute();
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void delete(final Repository repository, final String id) {
    checkNotNull(repository);
    checkNotNull(id);
    client.get().prepareDelete(repository.getName(), TYPE, id).execute();
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
      if (repository.facet(ViewFacet.class).isOnline()
          && securityHelper.allPermitted(new RepositoryViewPermission(repository, BreadActions.BROWSE))) {
        indexes.add(repository.getName());
      }
    }
    return indexes.toArray(new String[indexes.size()]);
  }

}
