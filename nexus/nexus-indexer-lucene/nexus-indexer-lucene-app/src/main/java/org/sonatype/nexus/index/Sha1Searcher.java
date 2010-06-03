package org.sonatype.nexus.index;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
                                                      Integer count, Integer hitLimit, SearchType searchType,
                                                      List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException
    {
        if ( !canHandle( terms ) )
        {
            return IteratorSearchResponse.EMPTY_RESPONSE;
        }

        return indexerManager.searchArtifactSha1ChecksumIterator( terms.get( TERM_SHA1 ), repositoryId, from, count,
            hitLimit, filters );
    }
}
