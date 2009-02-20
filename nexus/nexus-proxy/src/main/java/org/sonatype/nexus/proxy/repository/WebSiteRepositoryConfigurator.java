package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryWebSite;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

public interface WebSiteRepositoryConfigurator
{
    public WebSiteRepository updateRepositoryFromModel( WebSiteRepository old, ApplicationConfiguration configuration,
        CRepositoryWebSite repo, LocalRepositoryStorage ls )
        throws InvalidConfigurationException;
}
