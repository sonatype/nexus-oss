package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.modello.CRepository;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;

public class AbstractRepositoryConfigurationValidator
    implements RepositoryConfigurationValidator
{
    @Requirement
    private PlexusContainer plexusContainer;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    public void validate( ApplicationConfiguration configuration, CRepository repo )
        throws ConfigurationException
    {

    }

    // ==

    protected PlexusContainer getPlexusContainer()
    {
        return plexusContainer;
    }

    protected RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

    protected boolean existsRepositoryType( Class<?> role, String hint )
        throws InvalidConfigurationException
    {
        return componentExists( role, hint );
    }

    protected boolean existsLocalRepositoryStorage( String repoId, String provider )
        throws InvalidConfigurationException
    {
        return componentExists( LocalRepositoryStorage.class, provider );
    }

    protected boolean existsRemoteRepositoryStorage( String repoId, String provider )
        throws InvalidConfigurationException
    {
        return componentExists( RemoteRepositoryStorage.class, provider );
    }

    protected boolean componentExists( Class<?> role, String hint )
    {
        return getPlexusContainer().hasComponent( role, hint );
    }

}
