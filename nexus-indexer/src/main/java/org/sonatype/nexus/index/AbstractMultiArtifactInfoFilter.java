package org.sonatype.nexus.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.index.context.IndexingContext;

/**
 * An abstract helper class for implementing ArtifactInfoFilter that actually aggregates multiple filters into one. It
 * is up to developer to implement how will be they behave ("fail-fast", or "one-vote-enough for passing", etc).
 * 
 * @author cstamas
 */
public abstract class AbstractMultiArtifactInfoFilter
    implements ArtifactInfoFilter
{
    private final List<ArtifactInfoFilter> filters;

    public AbstractMultiArtifactInfoFilter( final List<ArtifactInfoFilter> filters )
    {
        if ( filters == null || filters.isEmpty() )
        {
            this.filters = null;
        }
        else
        {
            this.filters = new ArrayList<ArtifactInfoFilter>( filters );
        }
    }

    /**
     * Returns an unmodifiable list of filters.
     * 
     * @return
     */
    public List<ArtifactInfoFilter> getFilters()
    {
        if ( filters == null )
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.unmodifiableList( filters );
        }
    }

    /**
     * The filter's implementation is: if list of filters is empty, the just accept it, otherwise consult the list of
     * filters.
     */
    public boolean accepts( IndexingContext ctx, ArtifactInfo ai )
    {
        if ( this.filters == null )
        {
            return true;
        }
        else
        {
            return accepts( filters, ctx, ai );
        }
    }

    /**
     * It is left to final implementor to implement how we want to decide using filters. This method is called only if
     * we _have_ filters set!
     * 
     * @param filters
     * @param ctx
     * @param ai
     * @return
     */
    protected abstract boolean accepts( List<ArtifactInfoFilter> filters, IndexingContext ctx, ArtifactInfo ai );
}
