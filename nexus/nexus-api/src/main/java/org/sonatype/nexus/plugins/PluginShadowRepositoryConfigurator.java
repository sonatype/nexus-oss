package org.sonatype.nexus.plugins;

import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

/**
 * A component interface that may be implemented by plugins that wants to have some extra configuration capabilities on
 * Repository.
 * 
 * @author cstamas
 */
public interface PluginShadowRepositoryConfigurator
{
    boolean isHandledRepository( ShadowRepository repository );

    void configureRepository( ShadowRepository repository )
        throws InvalidConfigurationException;
}
