package org.sonatype.appcontext.source;

/**
 * Entry source marker describes the actual source of the key=values.
 * 
 * @author cstamas
 */
public interface EntrySourceMarker
{
    /**
     * Returns the description of this marker.
     * 
     * @return the description.
     */
    String getDescription();
}
