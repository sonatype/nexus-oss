package org.sonatype.appcontext.source;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;

/**
 * EntrySource that sources itself from System.getProperties().
 * 
 * @author cstamas
 */
public class SystemPropertiesEntrySource
    implements EntrySource, EntrySourceMarker
{
    public String getDescription()
    {
        return "system(properties)";
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Properties sysprops = System.getProperties();
        final Map<String, Object> result = new HashMap<String, Object>();
        for ( Map.Entry<Object, Object> entry : sysprops.entrySet() )
        {
            result.put( String.valueOf( entry.getKey() ), entry.getValue() );
        }
        return result;
    }
}
