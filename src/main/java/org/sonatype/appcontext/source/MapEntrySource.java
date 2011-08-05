package org.sonatype.appcontext.source;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

public class MapEntrySource
    implements EntrySource, EntrySourceMarker
{
    private final Map<String, Object> source;

    public MapEntrySource( final Map<String, Object> source )
    {
        this.source = Preconditions.checkNotNull( source );
    }

    public String getDescription()
    {
        return "map: " + source.toString();
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        for ( Map.Entry<String, Object> entry : source.entrySet() )
        {
            result.put( entry.getKey(), entry.getValue() );
        }

        return result;
    }
}
