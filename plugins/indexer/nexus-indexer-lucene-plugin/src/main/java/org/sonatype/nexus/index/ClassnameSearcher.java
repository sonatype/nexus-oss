/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.index;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.SearchType;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Searches Lucene index for artifacts containing classes with a specified name.
 *
 * @author Alin Dreghiciu
 */
@Component(role = Searcher.class, hint = "classname")
public class ClassnameSearcher
    implements Searcher
{

  /**
   * The key for class name term.
   */
  public static final String TERM_CLASSNAME = "cn";

  @Requirement
  private IndexerManager m_lucene;

  /**
   * Map should contain a term with key {@link #TERM_CLASSNAME} which has a non null value. {@inheritDoc}
   */
  public boolean canHandle(final Map<String, String> terms) {
    return terms.containsKey(TERM_CLASSNAME) && !StringUtils.isEmpty(terms.get(TERM_CLASSNAME));
  }

  public SearchType getDefaultSearchType() {
    return SearchType.SCORED;
  }

  /**
   * {@inheritDoc}
   */
  public FlatSearchResponse flatSearch(final Map<String, String> terms, final String repositoryId,
                                       final Integer from, final Integer count, final Integer hitLimit)
      throws NoSuchRepositoryException
  {
    if (!canHandle(terms)) {
      return new FlatSearchResponse(null, 0, Collections.<ArtifactInfo>emptySet());
    }
    return m_lucene.searchArtifactClassFlat(terms.get(TERM_CLASSNAME), repositoryId, from, count, hitLimit);
  }

  public IteratorSearchResponse flatIteratorSearch(Map<String, String> terms, String repositoryId, Integer from,
                                                   Integer count, Integer hitLimit, boolean uniqueRGA,
                                                   SearchType searchType,
                                                   List<ArtifactInfoFilter> filters)
      throws NoSuchRepositoryException
  {
    if (!canHandle(terms)) {
      return IteratorSearchResponse.empty(null);
    }

    return m_lucene.searchArtifactClassIterator(terms.get(TERM_CLASSNAME), repositoryId, from, count, hitLimit,
        searchType, filters);
  }

}