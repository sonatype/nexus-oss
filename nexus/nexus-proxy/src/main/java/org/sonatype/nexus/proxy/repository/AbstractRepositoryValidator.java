package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.Validator;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;

public class AbstractRepositoryValidator
    implements Validator
{
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    protected RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

    protected RepositoryTypeRegistry getRepositoryTypeRegistry()
    {
        return repositoryTypeRegistry;
    }

    public final void validate( ApplicationConfiguration configuration, Object repoConfig )
        throws InvalidConfigurationException
    {
        if ( repoConfig instanceof CRepository )
        {
            doValidate( configuration, (CRepository) repoConfig,
                        ( (CRepository) repoConfig ).externalConfigurationImple );
        }
    }

    protected void doValidate( ApplicationConfiguration configuration, CRepository repo,
                               ExternalConfiguration externalConfiguration )
        throws InvalidConfigurationException
    {
        // TODO:

    }
}
