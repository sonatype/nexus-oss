package org.sonatype.nexus.configuration;

/**
 * A Configurable component.
 * 
 * @author cstamas
 * @param <T>
 */
public interface Configurable<T>
{
    /**
     * Returns the current configuration of the component. May return null if there is not config object set.
     * 
     * @return
     */
    T getCurrentConfiguration();

    /**
     * Sets the configuration object and calls configure(). A shortcut for setCurrentConfiguration(config) and then
     * configure() calls.
     * 
     * @param config
     * @throws ConfigurationException
     */
    void configure( T config )
        throws ConfigurationException;

    /**
     * Makes the component to configure itself, if you tampered with the config object.
     * 
     * @throws ConfigurationException
     */
    void configure()
        throws ConfigurationException;
}
