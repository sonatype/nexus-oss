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
package org.sonatype.nexus.coreui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.BoolFilterBuilder
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.FilteredQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.indices.IndexMissingException
import org.elasticsearch.search.SearchHit
import org.sonatype.nexus.coreui.search.SearchContribution
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.StoreLoadParameters

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton
import javax.validation.ValidationException
import java.util.concurrent.TimeUnit

import static org.sonatype.nexus.repository.storage.StorageFacet.P_ATTRIBUTES
import static org.sonatype.nexus.repository.storage.StorageFacet.P_FORMAT
import static org.sonatype.nexus.repository.storage.StorageFacet.P_GROUP
import static org.sonatype.nexus.repository.storage.StorageFacet.P_NAME
import static org.sonatype.nexus.repository.storage.StorageFacet.P_REPOSITORY_NAME
import static org.sonatype.nexus.repository.storage.StorageFacet.P_VERSION

/**
 * Search {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_Search')
class SearchComponent
extends DirectComponentSupport
{

  static final String COMPONENT = 'component'

  @Inject
  Provider<Client> client

  @Inject
  Map<String, SearchContribution> searchContributions

  /**
   * Search based on configured filters and return search results grouped on group / name.
   *
   * @param parameters store parameters
   * @return search results
   */
  @DirectMethod
  @RequiresPermissions('nexus:repositories:read')
  List<SearchResultXO> read(final @Nullable StoreLoadParameters parameters) {
    QueryBuilder query = buildQuery(parameters)
    if (!query) {
      return null
    }

    SearchResponse response = client.get().prepareSearch()
        .setTypes(COMPONENT)
        .setQuery(query)
        .setScroll(new TimeValue(10, TimeUnit.SECONDS))
        .setSize(100)
        .execute()
        .actionGet()

    List<SearchResultXO> gas = []

      repeat:
    while (response.hits.hits.length > 0) {
      for (SearchHit hit : response.hits.hits) {
        if (gas.size() < 100) {
          // TODO check security
          def group = hit.source[P_GROUP]
          def name = hit.source[P_NAME]
          def ga = new SearchResultXO(
              id: "${group}:${name}",
              groupId: group,
              artifactId: name,
              format: hit.source[P_FORMAT]
          )
          if (!gas.contains(ga)) {
            gas.add(ga)
          }
        }
        else {
          break repeat
        }
      }
      response = client.get().prepareSearchScroll(response.getScrollId())
          .setScroll(new TimeValue(10, TimeUnit.SECONDS))
          .execute()
          .actionGet();
    }

    return gas
  }

  /**
   * Search based on configured filters and return versions / search result.
   * Search filters are expected to contain filters for group / name.
   *
   * @param parameters store parameters
   * @return version / search result
   */
  @DirectMethod
  @RequiresPermissions('nexus:repositories:read')
  List<SearchResultVersionXO> readVersions(final @Nullable StoreLoadParameters parameters) {
    QueryBuilder query = buildQuery(parameters)
    if (!query) {
      return null
    }

    SearchResponse response = client.get().prepareSearch()
        .setTypes(COMPONENT)
        .setQuery(query)
        .setScroll(new TimeValue(10, TimeUnit.SECONDS))
        .setSize(100)
        .execute()
        .actionGet()

    def versions = [] as SortedSet<SearchResultVersionXO>
    while (response.hits.hits.length > 0) {
      response.hits.hits.each { hit ->
        // TODO check security
        versions << new SearchResultVersionXO(
            groupId: hit.source[P_GROUP],
            artifactId: hit.source[P_NAME],
            version: hit.source[P_VERSION],
            repositoryId: hit.source[P_REPOSITORY_NAME],
            repositoryName: hit.source[P_REPOSITORY_NAME],
            name: hit.source[P_NAME],
            // FIXME: how we get the path
            path: hit.source[P_ATTRIBUTES]['raw']['path']
        )
      }
      response = client.get().prepareSearchScroll(response.getScrollId())
          .setScroll(new TimeValue(10, TimeUnit.SECONDS))
          .execute()
          .actionGet();
    }

    def versionOrder = 0
    return versions.collect { version ->
      version.versionOrder = versionOrder++
      return version
    }
  }

  /**
   * Builds a QueryBuilder based on configured filters.
   *
   * @param parameters store parameters
   */
  private QueryBuilder buildQuery(final StoreLoadParameters parameters) {
    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
    BoolFilterBuilder filterBuilder = FilterBuilders.boolFilter()
    parameters.filters?.each { filter ->
      SearchContribution contribution = searchContributions[filter.property]
      if (!contribution) {
        contribution = searchContributions['default']
      }
      contribution.contribute(queryBuilder, filter.property, filter.value)
      contribution.contribute(filterBuilder, filter.property, filter.value)
    }

    if (!queryBuilder.hasClauses() && !filterBuilder.hasClauses()) {
      return null
    }
    FilteredQueryBuilder query = QueryBuilders.filteredQuery(
        queryBuilder.hasClauses() ? queryBuilder : null,
        filterBuilder.hasClauses() ? filterBuilder : null
    )
    log.debug('Query: {}', query)

    try {
      if (!client.get().admin().indices().prepareValidateQuery().setQuery(query).execute().actionGet().valid) {
        throw new ValidationException("Invalid query")
      }
    }
    catch (IndexMissingException e) {
      // no repositories were created yet, so there is no point in searching
      return null;
    }

    return query
  }

}
