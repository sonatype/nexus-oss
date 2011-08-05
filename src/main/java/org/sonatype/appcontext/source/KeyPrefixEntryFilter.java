package org.sonatype.appcontext.source;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sonatype.appcontext.internal.Preconditions;

public class KeyPrefixEntryFilter
    implements EntryFilter
{
    private final List<String> prefixes;

    public KeyPrefixEntryFilter( final String... prefixes )
    {
        this( Arrays.asList( prefixes ) );
    }

    public KeyPrefixEntryFilter( final List<String> prefixes )
    {
        this.prefixes = Collections.unmodifiableList( Preconditions.checkNotNull( prefixes ) );
    }

    public boolean accept( final String key, final Object value )
    {
        if ( key != null )
        {
            for ( String prefix : prefixes )
            {
                if ( key.startsWith( prefix ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    public EntrySourceMarker getEntrySourceMarker( EntrySourceMarker source )
    {
        return new WrappingEntrySourceMarker( source )
        {
            @Override
            protected String getDescription( EntrySourceMarker wrapped )
            {
                return String.format( "keyPrefix(%s, %s)", prefixes, wrapped.getDescription() );
            }
        };
    }
}