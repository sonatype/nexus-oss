package org.sonatype.appcontext.source.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.WrappingEntrySourceMarker;

/**
 * EntryFilter that filters on key-prefix (keys are Strings) using key.startsWith() method, hence, this is case
 * sensitive! You can supply a list of key prefixes to accept.
 * 
 * @author cstamas
 */
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

    public EntrySourceMarker getFilteredEntrySourceMarker( final EntrySourceMarker source )
    {
        return new WrappingEntrySourceMarker( source )
        {
            @Override
            protected String getDescription( final EntrySourceMarker wrapped )
            {
                return String.format( "filter(keyStartsWith:%s, %s)", prefixes, wrapped.getDescription() );
            }
        };
    }
}