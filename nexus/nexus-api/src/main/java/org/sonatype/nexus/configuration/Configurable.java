package org.sonatype.nexus.configuration;

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
     * Makes the component to configure itself, if you tampered with the config object.
     * 
     * @throws ConfigurationException
     */
    void configure()
        throws ConfigurationException;

    /**
     * Returns true if there are some unsaved changes.
     * 
     * @return
     */
    boolean isDirty();
}
