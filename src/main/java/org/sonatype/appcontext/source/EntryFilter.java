package org.sonatype.appcontext.source;

public interface EntryFilter
{
    EntrySourceMarker getEntrySourceMarker( EntrySourceMarker source );

    boolean accept( String key, Object entry );
}
