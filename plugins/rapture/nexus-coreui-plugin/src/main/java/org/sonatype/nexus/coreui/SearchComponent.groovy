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
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.BoolFilterBuilder
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.FilteredQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHit
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.StoreLoadParameters
import org.sonatype.nexus.repository.search.SearchContribution

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

import static org.sonatype.nexus.repository.storage.StorageFacet.P_FORMAT
import static org.sonatype.nexus.repository.storage.StorageFacet.P_GROUP
import static org.sonatype.nexus.repository.storage.StorageFacet.P_NAME

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
   * Search based on configured filters and return search results grouped on GA level.
   *
   * @param parameters store parameters
   * @return paged search results
   */
  @DirectMethod
  @RequiresPermissions('nexus:repositories:read')
  List<SearchResultXO> read(final @Nullable StoreLoadParameters parameters) {

    SearchResponse response = client.get().prepareSearch()
        .setTypes(COMPONENT)
        .setQuery(buildQuery(parameters))
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
              id: "${group}:${name} (${hit.score})",
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
   * Search filters are expected to contain filters for groupid / artifactid.
   *
   * @param parameters store parameters
   * @return version / search result
   */
  @DirectMethod
  @RequiresPermissions('nexus:repositories:read')
  List<SearchResultVersionXO> readVersions(final @Nullable StoreLoadParameters parameters) {
    return null
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
    FilteredQueryBuilder query = QueryBuilders.filteredQuery(
        queryBuilder.hasClauses() ? queryBuilder : null,
        filterBuilder.hasClauses() ? filterBuilder : null
    )
    log.info('Query: {}', query)
    return query
  }

}
