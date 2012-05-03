package org.sonatype.appcontext.source;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;

/**
 * EntrySource that sources itself from {@code System.getenv()} call. It also may perform "normalization" of the keys,
 * as:
 * <ul>
 * <li>makes all keys lower case</li>
 * <li>replaces all occurrences of character '_' (underscore) to '.' (dot)</li>
 * </ul>
 * This is needed to make it possible to have different sources have same keys.
 * 
 * @author cstamas
 */
public class SystemEnvironmentEntrySource
    implements EntrySource, EntrySourceMarker
{
    public String getDescription()
    {
        return "system(env)";
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
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
}
