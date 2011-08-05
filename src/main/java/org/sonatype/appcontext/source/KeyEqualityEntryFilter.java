package org.sonatype.appcontext.source;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sonatype.appcontext.internal.Preconditions;

public class KeyEqualityEntryFilter
    implements EntryFilter
{
    private final List<String> keys;

    public KeyEqualityEntryFilter( final String... keys )
    {
        this( Arrays.asList( keys ) );
    }

    public KeyEqualityEntryFilter( final List<String> keys )
    {
        this.keys = Collections.unmodifiableList( Preconditions.checkNotNull( keys ) );
    }

    public boolean accept( final String key, final Object value )
    {
        return keys.contains( key );
    }

    public EntrySourceMarker getEntrySourceMarker( EntrySourceMarker source )
    {
        return new WrappingEntrySourceMarker( source )
        {
            @Override
            protected String getDescription( EntrySourceMarker wrapped )
            {
                return String.format( "filter(keyIsIn:%s, %s)", keys, wrapped.getDescription() );
            }
        };
    }
}
