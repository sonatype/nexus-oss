package org.sonatype.nexus.index;

import java.util.Iterator;

import org.apache.lucene.search.Query;

/**
 * A Search Response for the "iterator-like" search request. The totalHits reports _total_ hits found on index, even if
 * the set of ArtifactInfos are usually limited!
 * 
 * @author cstamas
 */
public class IteratorSearchResponse
    extends AbstractSearchResponse
    implements Iterable<ArtifactInfo>
{
    private final IteratorResultSet results;

    public IteratorSearchResponse( Query query, int totalHits, IteratorResultSet results )
    {
        super( query, totalHits );

        this.results = results;
    }

    public IteratorResultSet getResults()
    {
        return results;
    }

    public IteratorResultSet iterator()
    {
        return getResults();
    }

    /**
     * A helper method delegating the call to the IteratorResultSet.
     * 
     * @return
     */
    public int getTotalProcessedArtifactInfoCount()
    {
        return getResults().getTotalProcessedArtifactInfoCount();
    }

    // ==

    public static final IteratorResultSet EMPTY_ITERATOR_RESULT_SET = new IteratorResultSet()
    {
        public boolean hasNext()
        {
            return false;
        }

        public ArtifactInfo next()
        {
            return null;
        }

        public void remove()
        {
            throw new UnsupportedOperationException( "Method not supported on " + getClass().getName() );
        }

        public Iterator<ArtifactInfo> iterator()
        {
            return this;
        }

        public int getTotalProcessedArtifactInfoCount()
        {
            return 0;
        }
    };

    public static final IteratorSearchResponse EMPTY_ITERATOR_SEARCH_RESPONSE = new IteratorSearchResponse( null, 0,
        EMPTY_ITERATOR_RESULT_SET );

    public static final IteratorSearchResponse TOO_MANY_HITS_ITERATOR_SEARCH_RESPONSE = new IteratorSearchResponse(
        null, LIMIT_EXCEEDED, EMPTY_ITERATOR_RESULT_SET );

}
