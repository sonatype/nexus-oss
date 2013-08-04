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

@Component(role = Searcher.class, hint = "sha1")
public class Sha1Searcher
    implements Searcher
{
  public static final String TERM_SHA1 = "sha1";

  @Requirement
  private IndexerManager indexerManager;

  public boolean canHandle(Map<String, String> terms) {
    return (terms.containsKey(TERM_SHA1) && !StringUtils.isEmpty(terms.get(TERM_SHA1)));
  }

  public SearchType getDefaultSearchType() {
    return SearchType.EXACT;
  }

  public FlatSearchResponse flatSearch(Map<String, String> terms, String repositoryId, Integer from, Integer count,
                                       Integer hitLimit)
      throws NoSuchRepositoryException
  {
    // We do not support "old" style search anymore
    return new FlatSearchResponse(null, 0, Collections.<ArtifactInfo>emptySet());
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

    return indexerManager.searchArtifactSha1ChecksumIterator(terms.get(TERM_SHA1), repositoryId, from, count,
        hitLimit, filters);
  }
}
