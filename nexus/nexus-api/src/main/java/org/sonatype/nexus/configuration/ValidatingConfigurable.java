package org.sonatype.nexus.configuration;

/**
 * A Configurable component.
 * 
 * @author cstamas
 * @param <T>
 */
public interface ValidatingConfigurable<T>
    extends Configurable<T>
{
    /**
     * Validates the passed in configuration object.
     * 
     * @param config
     * @throws ConfigurationException
     */
    void validateConfiguration( T config )
        throws ConfigurationException;
}
