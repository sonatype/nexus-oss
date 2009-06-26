package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.ContentClass;

public abstract class AbstractGroupRepositoryValidator
    extends AbstractRepositoryValidator
{
    @Override
    public void doValidate( ApplicationConfiguration configuration, CRepository repo,
                            ExternalConfiguration externalConfiguration )
        throws InvalidConfigurationException
    {
        super.doValidate( configuration, repo, externalConfiguration );

        AbstractGroupRepositoryConfiguration extConf = (AbstractGroupRepositoryConfiguration) externalConfiguration;

        for ( String repoId : extConf.getMemberRepositoryIds() )
        {
            try
            {
                ContentClass myContentClass =
                    getRepositoryTypeRegistry().getRepositoryContentClass( repo.getProviderRole(),
                                                                           repo.getProviderHint() );

                Repository member = getRepositoryRegistry().getRepository( repoId );

                if ( !myContentClass.isCompatible( member.getRepositoryContentClass() ) )
                {
                    ValidationResponse response = new ApplicationValidationResponse();

                    ValidationMessage error =
                        new ValidationMessage( "repositories", "Repository has incompatible content type (needed='"
                            + myContentClass + "', got='" + member.getRepositoryContentClass() + "')",
                                               "Invalid content type" );

                    response.addValidationError( error );

                    throw new InvalidConfigurationException( response );
                }
            }
            catch ( NoSuchRepositoryException e )
            {
                ValidationResponse response = new ApplicationValidationResponse();

                ValidationMessage error =
                    new ValidationMessage( "repositories", e.getMessage(), "Invalid repository selected" );

                response.addValidationError( error );

                throw new InvalidConfigurationException( response );
            }
        }
    }
}
