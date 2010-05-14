package org.sonatype.nexus.index;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.MultiSearcher;
import org.sonatype.nexus.index.context.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.NexusIndexSearcher;

/**
 * Default implementation of IteratorResultSet.
 * 
 * @author cstamas
 */
public class DefaultIteratorResultSet
    implements IteratorResultSet
{
    /**
     * This is "hard limit", a possible maximum count of hits that Nexus Indexer will _serve_ even if asked for more.
     * Thus, it prevents some malicious attacks like forcing Nexus (or underlying IO to it's knees) but asking for huuge
     * count of hits. If anyone needs more than 1000 of hits, it should download the index and use Indexer API instead
     * to perform searches locally.
     */
    private static final int HARD_HIT_COUNT_LIMIT = 1000;

    private final ArtifactInfoFilter filter;

    private final ArtifactInfoPostprocessor postprocessor;

    private final MultiSearcher searcher;

    private final Hits hits;

    private final int from;

    private final int count;

    private final int maxRecPointer;

    private int pointer;

    private ArtifactInfo ai;

    protected DefaultIteratorResultSet( AbstractSearchRequest request, MultiSearcher searcher, final Hits hits )
        throws IOException
    {
        this.filter = request.getArtifactInfoFilter();

        this.postprocessor = request.getArtifactInfoPostprocessor();

        this.searcher = searcher;

        this.hits = hits;

        this.from = ( request.getStart() == AbstractSearchRequest.UNDEFINED ? 0 : request.getStart() );

        this.count =
            ( request.getCount() == AbstractSearchRequest.UNDEFINED ? HARD_HIT_COUNT_LIMIT : Math.min( request
                .getCount(), HARD_HIT_COUNT_LIMIT ) );

        this.pointer = from;

        this.maxRecPointer = from + count;

        ai = createNextAi();
    }

    public boolean hasNext()
    {
        return ai != null;
    }

    public ArtifactInfo next()
    {
        ArtifactInfo result = ai;

        try
        {
            ai = createNextAi();
        }
        catch ( IOException e )
        {
            ai = null;

            throw new IllegalStateException( "Cannot fetch next ArtifactInfo!", e );
        }

        return result;
    }

    protected ArtifactInfo createNextAi()
        throws IOException
    {
        ArtifactInfo result = null;

        // we should stop if:
        // a) we found what we want
        // b) pointer advanced over more documents that user requested
        // c) pointer advanced over more documents that hits has
        // or we found what we need
        while ( ( result == null ) && ( pointer < maxRecPointer ) && ( pointer < hits.length() ) )
        {
            Document doc = hits.doc( pointer );

            IndexingContext context = getIndexingContextForPointer( hits.id( pointer ) );

            result = IndexUtils.constructArtifactInfo( doc, context );

            if ( result != null )
            {
                result.repository = context.getRepositoryId();

                result.context = context.getId();

                if ( filter != null )
                {
                    if ( !filter.accepts( context, result ) )
                    {
                        result = null;
                    }
                }

                if ( result != null && postprocessor != null )
                {
                    postprocessor.postprocess( context, result );
                }
            }

            pointer++;
        }

        return result;
    }

    protected IndexingContext getIndexingContextForPointer( int docPtr )
    {
        return ( (NexusIndexSearcher) searcher.getSearchables()[searcher.subSearcher( docPtr )] ).getIndexingContext();
    }

    public void remove()
    {
        throw new UnsupportedOperationException( "Method not supported on " + getClass().getName() );
    }

    public Iterator<ArtifactInfo> iterator()
    {
        return this;
    }
}
