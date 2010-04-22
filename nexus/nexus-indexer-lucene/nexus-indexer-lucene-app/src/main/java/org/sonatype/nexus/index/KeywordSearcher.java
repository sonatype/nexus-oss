/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.index;

import java.util.Collections;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * Searches Lucene index wor artifacts that matches the provided keyword.
 * 
 * @author Alin Dreghiciu
 */
@Component( role = Searcher.class, hint = "keyword" )
public class KeywordSearcher
    implements Searcher
{

    /**
     * The key for keyword term.
     */
    public static final String TERM_KEYWORD = "q";

    @Requirement
    private IndexerManager m_lucene;

    /**
     * Map should contain a term with key {@link #TERM_KEYWORD} which has a non null value. {@inheritDoc}
     */
    public boolean canHandle( final Map<String, String> terms )
    {
        return terms.containsKey( TERM_KEYWORD ) && !StringUtils.isEmpty( terms.get( TERM_KEYWORD ) );
    }

    /**
     * {@inheritDoc}
     */
    public FlatSearchResponse flatSearch( final Map<String, String> terms, final String repositoryId,
                                          final Integer from, final Integer count, final Integer hitLimit )
        throws NoSuchRepositoryException
    {
        if ( !canHandle( terms ) )
        {
            return new FlatSearchResponse( null, 0, Collections.<ArtifactInfo> emptySet() );
        }
        return m_lucene.searchArtifactFlat( terms.get( TERM_KEYWORD ), repositoryId, from, count, hitLimit );
    }

    public IteratorSearchResponse flatIteratorSearch( Map<String, String> terms, String repositoryId, Integer from,
                                                      Integer count, Integer hitLimit, boolean uniqueRGA, boolean kwSearch )
        throws NoSuchRepositoryException
    {
        if ( !canHandle( terms ) )
        {
            return new IteratorSearchResponse( null, 0, null );
        }
        return m_lucene.searchArtifactIterator( terms.get( TERM_KEYWORD ), repositoryId, from, count, hitLimit, uniqueRGA, kwSearch );
    }

}
