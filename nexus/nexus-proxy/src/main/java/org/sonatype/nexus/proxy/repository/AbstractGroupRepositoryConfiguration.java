package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;

public class AbstractGroupRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    private static final String MEMBER_REPOSITORIES = "memberRepositories";

    public AbstractGroupRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public List<String> getMemberRepositoryIds()
    {
        return getCollection( getRootNode(), MEMBER_REPOSITORIES );
    }

    public void setMemberRepositoryIds( List<String> ids )
    {
        setCollection( getRootNode(), MEMBER_REPOSITORIES, ids );
    }

    public void clearMemberRepositoryIds()
    {
        List<String> empty = Collections.emptyList();

        setCollection( getRootNode(), MEMBER_REPOSITORIES, empty );
    }

    public void addMemberRepositoryId( String repositoryId )
    {
        addToCollection( getRootNode(), MEMBER_REPOSITORIES, repositoryId, true );
    }

    public void removeMemberRepositoryId( String repositoryId )
    {
        removeFromCollection( getRootNode(), MEMBER_REPOSITORIES, repositoryId );
    }

    @Override
    public ValidationResponse doValidateChanges( ApplicationConfiguration applicationConfiguration, CoreConfiguration owner, Xpp3Dom config )
    {
        ValidationResponse response = super.doValidateChanges( applicationConfiguration, owner, config );

        // validate members existence

        List<CRepository> allReposes = applicationConfiguration.getConfigurationModel().getRepositories();

        List<String> allReposesIds = new ArrayList<String>( allReposes.size() );

        for ( CRepository repository : allReposes )
        {
            allReposesIds.add( repository.getId() );
        }

        if ( !allReposesIds.containsAll( getMemberRepositoryIds() ) )
        {
            ValidationMessage message =
                new ValidationMessage( MEMBER_REPOSITORIES, "Group repository points to nonexistent members!",
                                       "The source nexus repository is not existing." );

            response.addValidationError( message );
        }
        
        return response;
    }
}
