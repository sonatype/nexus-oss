package org.sonatype.appcontext.source.keys;

import org.sonatype.appcontext.source.EntrySourceMarker;

/**
 * Key tranformer that may apply some transformation to the passed in key.
 * 
 * @author cstamas
 */
public interface KeyTransformer
{
    /**
     * Returns the transformed entry source marker.
     * 
     * @param source
     * @return
     */
    EntrySourceMarker getTransformedEntrySourceMarker( EntrySourceMarker source );

    /**
     * Performs the transformation of the key and returns the transformed key.
     * 
     * @param key to transform
     * @return transformed key
     */
    String transform( String key );
}
