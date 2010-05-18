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
 * Searches Lucene index for artifacts based on maven artifact coordinates.
 * 
 * @author Alin Dreghiciu
 */
@Component( role = Searcher.class, hint = "mavenCoordinates" )
public class MavenCoordinatesSearcher
    implements Searcher
{

    /**
     * The key for group term.
     */
    public static final String TERM_GROUP = "g";

    /**
     * The key for artifact term.
     */
    public static final String TERM_ARTIFACT = "a";

    /**
     * The key for version term.
     */
    public static final String TERM_VERSION = "v";

    /**
     * The key for packaging term.
     */
    public static final String TERM_PACKAGING = "p";

    /**
     * The key for classifier term.
     */
    public static final String TERM_CLASSIFIER = "c";

    @Requirement
    private IndexerManager m_lucene;

    /**
     * Map should contain a term with key {@link #TERM_GROUP} or {@link #TERM_ARTIFACT} or {@link #TERM_VERSION} or
     * {@link #TERM_PACKAGING} or {@link #TERM_CLASSIFIER} which has a non null value. {@inheritDoc}
     */
    public boolean canHandle( final Map<String, String> terms )
    {
        return ( terms.containsKey( TERM_GROUP ) && !StringUtils.isEmpty( terms.get( TERM_GROUP ) ) )
            || ( terms.containsKey( TERM_ARTIFACT ) && !StringUtils.isEmpty( terms.get( TERM_ARTIFACT ) ) )
            || ( terms.containsKey( TERM_VERSION ) && !StringUtils.isEmpty( terms.get( TERM_VERSION ) ) )
            || ( terms.containsKey( TERM_PACKAGING ) && !StringUtils.isEmpty( terms.get( TERM_PACKAGING ) ) )
            || ( terms.containsKey( TERM_CLASSIFIER ) && !StringUtils.isEmpty( terms.get( TERM_CLASSIFIER ) ) );
    }

    public SearchType getDefaultSearchType()
    {
        return SearchType.EXACT;
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
        return m_lucene.searchArtifactFlat( terms.get( TERM_GROUP ), terms.get( TERM_ARTIFACT ), terms
            .get( TERM_VERSION ), terms.get( TERM_PACKAGING ), terms.get( TERM_CLASSIFIER ), repositoryId, from, count,
            hitLimit );
    }

    public IteratorSearchResponse flatIteratorSearch( Map<String, String> terms, String repositoryId, Integer from,
                                                      Integer count, Integer hitLimit, boolean uniqueRGA, SearchType searchType )
        throws NoSuchRepositoryException
    {
        if ( !canHandle( terms ) )
        {
            return new IteratorSearchResponse( null, 0, null );
        }
        return m_lucene.searchArtifactIterator( terms.get( TERM_GROUP ), terms.get( TERM_ARTIFACT ), terms
            .get( TERM_VERSION ), terms.get( TERM_PACKAGING ), terms.get( TERM_CLASSIFIER ), repositoryId, from, count,
            hitLimit, searchType );
    }

}