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
import org.elasticsearch.index.query.BoolFilterBuilder
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.FilteredQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHit
import org.sonatype.nexus.coreui.search.SearchContribution
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.StoreLoadParameters
import org.sonatype.nexus.repository.search.CustomFilterBuilders
import org.sonatype.nexus.repository.search.SearchService

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.ValidationException

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

  @Inject
  SearchService searchService

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

    List<SearchResultXO> gas = []
    for (SearchHit hit : browse(query)) {
      if (gas.size() < 100) {
        def group = hit.source[P_GROUP]
        def name = hit.source[P_NAME]
        def ga = new SearchResultXO(
            groupingKey: group ? "${group}:${name}" : name,
            group: group,
            name: name,
            format: hit.source[P_FORMAT]
        )
        if (!gas.contains(ga)) {
          gas.add(ga)
        }
      }
      else {
        break
      }
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

    def versions = [] as SortedSet<SearchResultVersionXO>
    browse(query).each { hit ->
      def group = hit.source[P_GROUP]
      def name = hit.source[P_NAME]
      versions << new SearchResultVersionXO(
          groupingKey: group ? "${group}:${name}" : name,
          group: group,
          name: name,
          version: hit.source[P_VERSION],
          repositoryId: hit.source[P_REPOSITORY_NAME],
          repositoryName: hit.source[P_REPOSITORY_NAME],
          // FIXME: how we get the path
          //path: hit.source[P_ATTRIBUTES]['raw']['path']
      )
    }

    def versionOrder = 0
    return versions.collect { version ->
      version.versionOrder = versionOrder++
      return version
    }
  }

  private Iterable<SearchHit> browse(final QueryBuilder query) {
    try {
      return searchService.browse(query)
    }
    catch (IllegalArgumentException e) {
      throw new ValidationException(e.getMessage())
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
    filterBuilder.must(CustomFilterBuilders.visibleToCurrentUserFilter());
    FilteredQueryBuilder query = QueryBuilders.filteredQuery(
        queryBuilder.hasClauses() ? queryBuilder : null,
        filterBuilder.hasClauses() ? filterBuilder : null
    )
    log.debug('Query: {}', query)

    return query
  }

}
