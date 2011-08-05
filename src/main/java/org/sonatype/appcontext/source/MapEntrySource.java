package org.sonatype.appcontext.source;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

public class MapEntrySource
    implements EntrySource, EntrySourceMarker
{
    private final String name;

    private final Map<?, ?> source;

    public MapEntrySource( final Map<?, ?> source )
    {
        this( "map", source );
    }

    public MapEntrySource( final String name, final Map<?, ?> source )
    {
        this.name = Preconditions.checkNotNull( name );
        this.source = Preconditions.checkNotNull( source );
    }

    public String getDescription()
    {
        return String.format( "map: %s (size:%s)", name, source.size() );
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        for ( Map.Entry<?, ?> entry : source.entrySet() )
        {
            if ( entry.getValue() != null )
            {
                result.put( String.valueOf( entry.getKey() ), String.valueOf( entry.getValue() ) );
            }
            else
            {
                result.put( String.valueOf( entry.getKey() ), null );
            }
        }

        return result;
    }
}
