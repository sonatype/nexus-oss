package org.sonatype.nexus.configuration;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;

/**
 * Validator component.
 * 
 * @author cstamas
 */
public interface Validator
{
    /**
     * Validates the repoConfig.
     * 
     * @param configuration
     * @param model
     * @throws ConfigurationException on validation problem.
     */
    public void validate( ApplicationConfiguration configuration, Object model )
        throws InvalidConfigurationException;
}
