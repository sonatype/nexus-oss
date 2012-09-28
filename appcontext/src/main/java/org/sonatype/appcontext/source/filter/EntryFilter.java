package org.sonatype.appcontext.source.filter;

import org.sonatype.appcontext.source.EntrySourceMarker;

/**
 * A filter for entries.
 * 
 * @author cstamas
 */
public interface EntryFilter
{
    /**
     * Returns the filtered entry source marker.
     * 
     * @param source
     * @return
     */
    EntrySourceMarker getFilteredEntrySourceMarker( EntrySourceMarker source );

    /**
     * Returns true if the key and entry is acceptable by this filter, otherwise false.
     * 
     * @param key
     * @param entry
     * @return true to accept or false to filter out the passed in key-value.
     */
    boolean accept( String key, Object entry );
}
