package org.sonatype.sample.wrapper;

import java.io.File;
import java.io.IOException;

/**
 * Simple interface for persisted configuration files.
 * 
 * @author cstamas
 */
public interface PersistedConfiguration
{
    /**
     * Resets the configuration by reloading original wrapper.conf it is pointed to. Looses all changes made so fat not
     * saved.
     * 
     * @throws IOException
     */
    void reset()
        throws IOException;

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
}
