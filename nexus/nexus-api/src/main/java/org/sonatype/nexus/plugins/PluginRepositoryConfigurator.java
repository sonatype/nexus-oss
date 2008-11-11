package org.sonatype.nexus.plugins;

import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A component interface that may be implemented by plugins that wants to have some extra configuration capabilities on
 * Repository.
 * 
 * @author cstamas
 */
public interface PluginRepositoryConfigurator
{
    boolean isHandledRepository( Repository repository );

    void configureRepository( Repository repository )
        throws InvalidConfigurationException;
}
