package org.sonatype.appcontext.source;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

public class AppContextEntrySource
    implements EntrySource, EntrySourceMarker
{
    private final AppContext context;

    public AppContextEntrySource( final AppContext context )
    {
        this.context = Preconditions.checkNotNull( context );
    }

    public String getDescription()
    {
        return "appcontext: " + context.getId();
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, Object> result = new HashMap<String, Object>( context.size() );

        for ( Map.Entry<String, Object> entry : context.entrySet() )
        {
            result.put( entry.getKey(), entry.getValue() );
        }

        return result;
    }
}
