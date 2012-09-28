package org.sonatype.appcontext.source.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.WrappingEntrySourceMarker;

/**
 * EntryFilter that filters on key-equality (keys are Strings) using key.equals() method, hence, this is case sensitive!
 * You can supply a list of keys to accept.
 * 
 * @author cstamas
 */
public class KeyEqualityEntryFilter
    implements EntryFilter
{
    /**
     * The list of keys to accept.
     */
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

    public EntrySourceMarker getFilteredEntrySourceMarker( final EntrySourceMarker source )
    {
        return new WrappingEntrySourceMarker( source )
        {
            @Override
            protected String getDescription( final EntrySourceMarker wrapped )
            {
                return String.format( "filter(keyIsIn:%s, %s)", keys, wrapped.getDescription() );
            }
        };
    }
}
