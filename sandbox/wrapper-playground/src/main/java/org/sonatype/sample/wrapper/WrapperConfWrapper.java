package org.sonatype.sample.wrapper;

import java.io.File;
import java.io.IOException;

/**
 * The "low level" abstraction around wrapper.conf, or any other Java properties-like file that uses similar notation
 * for lists like wrapper.conf does (".1 .2 etc" suffixes to keys).
 * 
 * @author cstamas
 */
public interface WrapperConfWrapper
{
    /**
     * Resets the editor by reloading original wrapper.conf it is pointed to. Looses all changes made so fat not saved.
     */
    void reset();

    /**
     * Persists back the loaded wrapper.conf. reset() will not revert to original!
     * 
     * @throws IOException
     */
    void save()
        throws IOException;

    /**
     * Persists the wrapper.conf to the supplied file, not overwriting the original. reset() will revert to original!
     * 
     * @throws IOException
     */
    void save( File target )
        throws IOException;

    /**
     * Getter for "single-keyed" properties. Will not look for ".1 .2, etc" suffixes.
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    String getProperty( String key, String defaultValue );

    /**
     * Setter for "single-keyed" properties. Will not try to figure out ".1 .2 etc suffixes.
     * 
     * @param key
     * @param value
     */
    void setProperty( String key, String value );

    /**
     * Getter for "multi-keyed" properties. Here, the key is appended by ".1 .2 etc" suffixes to read the values, as
     * needed.
     * 
     * @param key
     * @return
     */
    String[] getPropertyList( String key );

    /**
     * Setter for "multi-keyed" properties. Here, the key will be appended with ".1 .2 etc" suffixes when written, as
     * needed. Supplying null as values, or empty array WILL DELETE the settings.
     * 
     * @param key
     * @param values the values to write to. If null or empty array, it will result in DELETION of params.
     */
    void setPropertyList( String key, String[] values );
}
