/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.maven.index.ArtifactInfo
import org.apache.maven.index.IteratorSearchResponse
import org.apache.maven.index.MAVEN
import org.apache.maven.index.SearchType
import org.apache.maven.index.artifact.GavCalculator
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.PagedResponse
import org.sonatype.nexus.extdirect.model.StoreLoadParameters
import org.sonatype.nexus.index.IndexerManager

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

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
  IndexerManager indexerManager

  @Inject
  @Named('maven2')
  GavCalculator gavCalculator

  @DirectMethod
  PagedResponse<SearchResultXO> read(final @Nullable StoreLoadParameters parameters) {

    BooleanQuery bq = new BooleanQuery()
    def keywordOccur = BooleanClause.Occur.SHOULD

    if (parameters.getFilter('groupid')) {
      bq.add(
          indexerManager.constructQuery(MAVEN.GROUP_ID, parameters.getFilter('groupid'), SearchType.EXACT),
          BooleanClause.Occur.MUST
      );
      keywordOccur = BooleanClause.Occur.MUST
    }
    if (parameters.getFilter('artifactid')) {
      bq.add(
          indexerManager.constructQuery(MAVEN.ARTIFACT_ID, parameters.getFilter('artifactid'), SearchType.EXACT),
          BooleanClause.Occur.MUST
      );
      keywordOccur = BooleanClause.Occur.MUST
    }
    if (parameters.getFilter('version')) {
      bq.add(
          indexerManager.constructQuery(MAVEN.VERSION, parameters.getFilter('version'), SearchType.EXACT),
          BooleanClause.Occur.MUST
      );
    }
    if (parameters.getFilter('classifier')) {
      bq.add(
          indexerManager.constructQuery(MAVEN.CLASSIFIER, parameters.getFilter('classifier'), SearchType.EXACT),
          BooleanClause.Occur.MUST
      );
    }
    if (parameters.getFilter('packaging')) {
      bq.add(
          indexerManager.constructQuery(MAVEN.PACKAGING, parameters.getFilter('packaging'), SearchType.EXACT),
          BooleanClause.Occur.MUST
      );
    }
    if (parameters.getFilter('classname')) {
      bq.add(
          indexerManager.constructQuery(MAVEN.CLASSNAMES, parameters.getFilter('classname'), SearchType.SCORED),
          BooleanClause.Occur.MUST
      );
    }
    if (parameters.getFilter('sha-1')) {
      def sha1 = parameters.getFilter('sha-1')
      bq.add(
          indexerManager.constructQuery(MAVEN.SHA1, sha1, sha1.length() < 40 ? SearchType.SCORED : SearchType.EXACT),
          BooleanClause.Occur.MUST
      );
    }
    if (parameters.getFilter('keyword')) {
      def keyword = parameters.getFilter('keyword')
      if (!parameters.getFilter('groupid')) {
        bq.add(
            indexerManager.constructQuery(MAVEN.GROUP_ID, keyword, SearchType.SCORED),
            keywordOccur
        );
      }
      if (!parameters.getFilter('artifactid')) {
        bq.add(
            indexerManager.constructQuery(MAVEN.ARTIFACT_ID, keyword, SearchType.SCORED),
            keywordOccur
        );
      }
    }

    IteratorSearchResponse iterator = indexerManager.searchQueryIterator(
        bq, null, parameters.start, parameters.limit, null, false, null
    )

    List<SearchResultXO> results = [];
    for (ArtifactInfo info : iterator) {
      results << new SearchResultXO(
          repositoryId: info.repository,
          uri: 'maven:' + info.groupId + ':' + info.artifactId,
          version: info.version,
          path: gavCalculator.gavToPath(info.calculateGav())
      )
    }

    log.info('Query: {}, Hits: {}', bq, iterator.totalHitsCount)

    iterator.close()

    return new PagedResponse<SearchResultXO>(iterator.totalHitsCount, results)
  }

}
