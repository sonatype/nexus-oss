package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public interface ShadowRepositoryConfigurator
{
    public ShadowRepository updateRepositoryFromModel( ShadowRepository old, ApplicationConfiguration configuration,
        CRepositoryShadow repo, RemoteStorageContext rsc, LocalRepositoryStorage ls, Repository masterRepository )
        throws InvalidConfigurationException;
}
