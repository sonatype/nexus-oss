package org.sonatype.nexus.configuration;

import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.plugin.ExtensionPoint;

/**
 * Validator component.
 * 
 * @author cstamas
 */
@ExtensionPoint
@Singleton
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
