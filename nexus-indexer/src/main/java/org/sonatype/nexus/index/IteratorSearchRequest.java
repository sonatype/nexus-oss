package org.sonatype.nexus.index;

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A Search Request that will perform the new "iterator-like" type of search.
 * 
 * @author cstamas
 */
public class IteratorSearchRequest
    extends AbstractSearchRequest
{
    public IteratorSearchRequest( Query query )
    {
        this( query, null, null );
    }

    public IteratorSearchRequest( Query query, ArtifactInfoFilter filter )
    {
        this( query, null, filter );
    }

    public IteratorSearchRequest( Query query, IndexingContext context )
    {
        this( query, context != null ? Arrays.asList( new IndexingContext[] { context } ) : null, null );
    }

    public IteratorSearchRequest( Query query, List<IndexingContext> contexts )
    {
        this( query, contexts, null );
    }

    public IteratorSearchRequest( Query query, List<IndexingContext> contexts, ArtifactInfoFilter filter )
    {
        super( query, contexts );

        setArtifactInfoFilter( filter );
    }
}
