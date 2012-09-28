package org.sonatype.appcontext.source;

import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;

/**
 * A EntrySource provides key=values from various sources, that will make into the AppContext.
 * 
 * @author cstamas
 */
public interface EntrySource
{
    /**
     * Returns the entry source marker.
     * 
     * @return
     */
    EntrySourceMarker getEntrySourceMarker();

    /**
     * Returns a map of key=values to have them put into the AppContext.
     * 
     * @param request
     * @return a map of key=vqlues to be put into AppContext.
     * @throws AppContextException
     */
    Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException;
}
