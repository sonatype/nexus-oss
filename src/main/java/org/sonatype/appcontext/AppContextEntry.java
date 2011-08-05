package org.sonatype.appcontext;

import org.sonatype.appcontext.source.EntrySourceMarker;

public interface AppContextEntry
{
    String getKey();
    
    Object getValue();
    
    Object getRawValue();

    EntrySourceMarker getEntrySourceMarker();
}
