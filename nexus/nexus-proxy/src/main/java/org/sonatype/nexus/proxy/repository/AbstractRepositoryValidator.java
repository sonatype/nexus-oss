package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.Validator;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;

public abstract class AbstractRepositoryValidator
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
            prepareExternalConfiguration( (CRepository) repoConfig );

            doValidate( configuration, (CRepository) repoConfig,
                        ( (CRepository) repoConfig ).externalConfigurationImple );
        }
        else
        {
            throw new InvalidConfigurationException( "The Repository configuration is not an instance of "
                + CRepository.class.getName() );
        }
    }

    protected void doValidate( ApplicationConfiguration configuration, CRepository repo,
                               ExternalConfiguration externalConfiguration )
        throws InvalidConfigurationException
    {
        // TODO:

    }

    // ==

    protected void prepareExternalConfiguration( CRepository repoConfig )
    {
        if ( repoConfig.getExternalConfiguration() == null )
        {
            // just put an elephant in South Africa to find it for sure ;)
            repoConfig.setExternalConfiguration( new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME ) );
        }

        if ( repoConfig.externalConfigurationImple == null )
        {
            // in 1st round, i intentionally choosed to make our lives bitter, and handle config manually
            // later we will see about it
            repoConfig.externalConfigurationImple =
                createExternalConfiguration( (Xpp3Dom) repoConfig.getExternalConfiguration() );
        }
    }

    protected abstract ExternalConfiguration createExternalConfiguration( Xpp3Dom dom );

    public ExternalConfiguration getExternalConfiguration( Repository repository )
    {
        return repository.getCurrentCoreConfiguration().getExternalConfiguration();
    }
}
