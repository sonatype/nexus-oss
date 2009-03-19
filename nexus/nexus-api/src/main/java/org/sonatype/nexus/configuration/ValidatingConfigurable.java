package org.sonatype.nexus.configuration;

/**
 * A Configurable component.
 * 
 * @author cstamas
 * @param <T>
 */
public interface ValidatingConfigurable
    extends Configurable
{
    /**
     * Validates the passed in configuration object.
     * 
     * @param config
     * @throws ConfigurationException
     */
    void validateConfiguration( Object config )
        throws ConfigurationException;
}
