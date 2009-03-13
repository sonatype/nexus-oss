package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.modello.CRepository;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

public class AbstractGroupRepositoryConfigurator
    extends AbstractRepositoryConfigurator
{
    public static final String MEMBER_REPOSITORIES = "memberRepositories";

    @Override
    public void doConfigure( Repository repository, ApplicationConfiguration configuration, CRepository repo,
        PlexusConfiguration externalConfiguration )
        throws ConfigurationException
    {
        super.doConfigure( repository, configuration, repo, externalConfiguration );

        List<String> members = new ArrayList<String>( externalConfiguration
            .getChild( MEMBER_REPOSITORIES ).getChildCount() );

        try
        {
            for ( PlexusConfiguration config : externalConfiguration.getChild( MEMBER_REPOSITORIES ).getChildren() )
            {
                members.add( config.getValue() );
            }
        }
        catch ( PlexusConfigurationException e )
        {
            throw new InvalidConfigurationException( "Cannot read configuration!" );
        }

        for ( String repoId : members )
        {
            try
            {
                Repository member = getRepositoryRegistry().getRepository( repoId );

                if ( !repository.getRepositoryContentClass().isCompatible( member.getRepositoryContentClass() ) )
                {
                    ValidationResponse response = new ApplicationValidationResponse();

                    ValidationMessage error = new ValidationMessage(
                        "repositories",
                        "Repository has incompatible content type",
                        "Invalid content type" );

                    response.addValidationError( error );

                    throw new InvalidConfigurationException( response );
                }
            }
            catch ( NoSuchRepositoryException e )
            {
                ValidationResponse response = new ApplicationValidationResponse();

                ValidationMessage error = new ValidationMessage(
                    "repositories",
                    e.getMessage(),
                    "Invalid repository selected" );

                response.addValidationError( error );

                throw new InvalidConfigurationException( response );
            }
        }

        repository.adaptToFacet( GroupRepository.class ).setMemberRepositories( members );
    }
}
