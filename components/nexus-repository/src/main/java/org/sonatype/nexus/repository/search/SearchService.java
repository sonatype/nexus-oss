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

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.Component;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;

/**
 * Search service.
 *
 * @since 3.0
 */
public interface SearchService
{

  /**
   * Create component metadata index for specified repository, if does not already exits.
   */
  void createIndex(Repository repository);

  /**
   * Deletes component metadata index for specified repository.
   */
  void deleteIndex(Repository repository);

  /**
   * Index component metadata.
   */
  void put(Repository repository, Component component);

  /**
   * Remove component metadata from index.
   */
  void delete(Repository repository, Component component);

  /**
   * Search component metadata and browse results.
   */
  Iterable<SearchHit> browse(QueryBuilder query);

  /**
   * Search component metadata and browse results (paged).
   */
  Iterable<SearchHit> browse(QueryBuilder query, int from, int size);

  /**
   * Search component metadata and browse results (paged).
   */
  SearchResponse search(QueryBuilder query, int from, int size);

}
