/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.index;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.SearchType;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

@Component( role = Searcher.class, hint = "sha1" )
public class Sha1Searcher
    implements Searcher
{
    public static final String TERM_SHA1 = "sha1";

    @Requirement
    private IndexerManager indexerManager;

    public boolean canHandle( Map<String, String> terms )
    {
        return ( terms.containsKey( TERM_SHA1 ) && !StringUtils.isEmpty( terms.get( TERM_SHA1 ) ) );
    }

    public SearchType getDefaultSearchType()
    {
        return SearchType.EXACT;
    }

    public FlatSearchResponse flatSearch( Map<String, String> terms, String repositoryId, Integer from, Integer count,
                                          Integer hitLimit )
        throws NoSuchRepositoryException
    {
        // We do not support "old" style search anymore
        return new FlatSearchResponse( null, 0, Collections.<ArtifactInfo> emptySet() );
    }

    public IteratorSearchResponse flatIteratorSearch( Map<String, String> terms, String repositoryId, Integer from,
                                                      Integer count, Integer hitLimit, boolean uniqueRGA, SearchType searchType,
                                                      List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException
    {
        if ( !canHandle( terms ) )
        {
            return IteratorSearchResponse.empty( null );
        }

        return indexerManager.searchArtifactSha1ChecksumIterator( terms.get( TERM_SHA1 ), repositoryId, from, count,
            hitLimit, filters );
    }
}
