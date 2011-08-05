package org.sonatype.appcontext.source;

import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;

public interface EntrySource
{
    EntrySourceMarker getEntrySourceMarker();
    
    Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException;
}
