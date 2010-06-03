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

    public Iterator<ArtifactInfo> iterator()
    {
        return getResults();
    }

    // ==

    public static final IteratorSearchResponse EMPTY_RESPONSE =
        new IteratorSearchResponse( null, 0, new IteratorResultSet()
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
        } );

}
