package org.sonatype.appcontext.source;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;

public class SystemEnvironmentEntrySource
    implements EntrySource, EntrySourceMarker
{
    public String getDescription()
    {
        return "system:env";
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, String> envMap = System.getenv();

        final Map<String, Object> result = new HashMap<String, Object>();

        for ( Map.Entry<String, String> entry : envMap.entrySet() )
        {
            // MAVEN_OPTS => maven.opts
            final String key = entry.getKey().toLowerCase().replace( '_', '.' );

            result.put( key, entry.getValue() );
        }

        return result;
    }
}
