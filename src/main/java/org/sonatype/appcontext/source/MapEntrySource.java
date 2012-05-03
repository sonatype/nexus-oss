package org.sonatype.appcontext.source;

import java.util.Map;

import org.sonatype.appcontext.internal.Preconditions;

/**
 * An EntrySource that is sourced from a {@code java.util.Map}.
 * 
 * @author cstamas
 */
public class MapEntrySource
    extends AbstractMapEntrySource
{
    private final Map<?, ?> source;

    public MapEntrySource( final String name, final Map<?, ?> source )
    {
        super( name, "map" );
        this.source = Preconditions.checkNotNull( source );
    }

    @Override
    protected Map<?, ?> getSource()
    {
        return source;
    }
}
