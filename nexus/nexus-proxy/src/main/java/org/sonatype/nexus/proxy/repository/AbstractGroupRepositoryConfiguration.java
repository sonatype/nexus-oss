package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;

public class AbstractGroupRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    private static final String MEMBER_REPOSITORIES = "memberRepositories";

    public AbstractGroupRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public boolean membersChanged()
    {
        String oldConf = getCollection( getRootNode(), MEMBER_REPOSITORIES ).toString();

        String newConf = getCollection( getRootNode(), MEMBER_REPOSITORIES ).toString();

        return !StringUtils.equals( oldConf, newConf );
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
    public void validate( ApplicationConfiguration applicationConfiguration, CoreConfiguration owner )
        throws ConfigurationException
    {
        super.validate( applicationConfiguration, owner );

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

            ValidationResponse response = new ApplicationValidationResponse();

            response.addValidationError( message );

            throw new InvalidConfigurationException( response );
        }
    }
}
