package org.sonatype.appcontext.source;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

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
        return String.format( "static: \"%s\"=\"%s\"", key, value );
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
