package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

public interface GroupRepositoryConfigurator
{

    GroupRepository updateRepositoryFromModel( GroupRepository old, ApplicationConfiguration nexusConfiguration,
        CRepositoryGroup group, LocalRepositoryStorage ls ) throws InvalidConfigurationException;

}
