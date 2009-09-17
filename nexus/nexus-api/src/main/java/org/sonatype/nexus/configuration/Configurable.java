package org.sonatype.nexus.configuration;

import org.sonatype.configuration.ConfigurationException;


/**
 * A Configurable component.
 * 
 * @author cstamas
 */
public interface Configurable
{
    /**
     * Returns the current core configuration of the component.May return null if there is not config object set.
     * 
     * @return
     */
    CoreConfiguration getCurrentCoreConfiguration();

    /**
     * Sets the configuration object and calls configure(). A shortcut for setCurrentConfiguration(config) and then
     * configure() calls.
     * 
     * @param config
     * @throws ConfigurationException
     */
    void configure( Object config )
        throws ConfigurationException;

    /**
     * Returns true if there are some unsaved changes.
     * 
     * @return
     */
    boolean isDirty();

    /**
     * Commits the changes. Resets the state of config "back to normal" (saved).
     */
    boolean commitChanges()
        throws ConfigurationException;

    /**
     * Rollbacks the changes. Resets the state of config "back to normal" (saved).
     */
    boolean rollbackChanges();
    
    /**
     * A simple short name.
     */
    String getName();
}
