package org.sonatype.appcontext.source;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * A static EntrySource that holds the key and value to make it into AppContext. Useful in testing, or when you need to
 * add one key=value into context, and you need to calculate those somehow before constructing AppContext.
 * 
 * @author cstamas
 */
public class StaticEntrySource
    implements EntrySource, EntrySourceMarker
{
    private final String key;

    private final Object value;

    public StaticEntrySource( final String key, final Object val )
    {
        this.key = Preconditions.checkNotNull( key );
        this.value = val;
    }

    public String getDescription()
    {
        if ( value != null )
        {
            return String.format( "static(\"%s\"=\"%s\")", key, String.valueOf( value ) );
        }
        else
        {
            return String.format( "static(\"%s\"=null)", key );
        }
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, Object> result = new HashMap<String, Object>( 1 );
        result.put( key, value );
        return result;
    }
}
