/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration.application.runtime;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.GroupRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.repository.ShadowRepositoryConfigurator;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;

/**
 * The Class DefaultRuntimeConfigurationBuilder. Todo: all the bad thing is now concentrated in this class. We are
 * playing container instead of container.
 * 
 * @author cstamas
 */
@Component( role = ApplicationRuntimeConfigurationBuilder.class )
public class DefaultApplicationRuntimeConfigurationBuilder
    extends AbstractLogEnabled
    implements ApplicationRuntimeConfigurationBuilder, Contextualizable
{
    public static final String DEFAULT_LS_PROVIDER = "file";

    private static final String DEFAULT_REPOSITORY_TYPE = "maven2";

    private static final String DEFAULT_GROUPREPOSITORY_TYPE = "maven2";

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    private PlexusContainer plexusContainer;

    private NexusConfiguration nexusConfiguration;

    public void contextualize( Context ctx )
        throws ContextException
    {
        this.plexusContainer = (PlexusContainer) ctx.get( PlexusConstants.PLEXUS_KEY );
    }

    public void initialize( NexusConfiguration configuration )
    {
        this.nexusConfiguration = configuration;
    }

    public Repository createRepositoryFromModel( Configuration configuration, CRepository repo )
        throws InvalidConfigurationException
    {
        Repository repository = createRepository( Repository.class, repo.getType() );

        return updateRepositoryFromModel( repository, configuration, repo );
    }

    public Repository updateRepositoryFromModel( Repository old, Configuration configuration, CRepository repo )
        throws InvalidConfigurationException
    {
        Repository repository = null;

        String type = repo.getType();

        if ( type == null )
        {
            type = DEFAULT_REPOSITORY_TYPE;
        }

        try
        {
            LocalRepositoryStorage ls = null;

            if ( repo.getLocalStorage() != null )
            {
                ls = getLocalRepositoryStorage( repo.getId(), repo.getLocalStorage().getProvider() );
            }
            else
            {
                ls = getLocalRepositoryStorage( repo.getId(), DEFAULT_LS_PROVIDER );
            }

            RemoteRepositoryStorage rs = null;

            if ( repo.getRemoteStorage() != null )
            {
                rs = getRemoteRepositoryStorage( repo.getId(), repo.getRemoteStorage().getProvider() );
            }

            // Setting contentClass specific things on a repository
            RepositoryConfigurator configurator = plexusContainer.lookup(
                RepositoryConfigurator.class,
                type );

            repository = configurator.updateRepositoryFromModel( old, nexusConfiguration, repo, nexusConfiguration
                .getRemoteStorageContext(), ls, rs );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Repository of type='" + type
                + "' does not have a valid configurator!", e );
        }

        return repository;
    }

    public ShadowRepository createRepositoryFromModel( Configuration configuration, CRepositoryShadow shadow )
        throws InvalidConfigurationException
    {
        ShadowRepository shadowRepository = createRepository( ShadowRepository.class, shadow.getType() );

        return updateRepositoryFromModel( shadowRepository, configuration, shadow );
    }

    public ShadowRepository updateRepositoryFromModel( ShadowRepository old, Configuration configuration,
        CRepositoryShadow shadow )
        throws InvalidConfigurationException
    {
        ShadowRepository shadowRepository = null;

        Repository master = null;

        try
        {
            master = repositoryRegistry.getRepository( shadow.getShadowOf() );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new InvalidConfigurationException( "Shadow repository points to a nonexistent master with ID="
                + shadow.getShadowOf() );
        }

        try
        {

            // Setting contentClass specific things on a repository
            ShadowRepositoryConfigurator configurator = plexusContainer.lookup(
                ShadowRepositoryConfigurator.class,
                shadow.getType() );

            shadowRepository = configurator.updateRepositoryFromModel(
                old,
                nexusConfiguration,
                shadow,
                nexusConfiguration.getRemoteStorageContext(),
                getLocalRepositoryStorage( shadow.getId(), DEFAULT_LS_PROVIDER ),
                master );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Shadow repository of type='" + shadow.getType()
                + "' does not have a valid configurator!", e );
        }

        return shadowRepository;
    }

    public GroupRepository createRepositoryFromModel( Configuration configuration, CRepositoryGroup group )
        throws InvalidConfigurationException
    {
        if ( group.getType() == null )
        {
            group.setType( DEFAULT_GROUPREPOSITORY_TYPE );
        }

        GroupRepository groupRepository = createRepository( GroupRepository.class, group.getType() );

        return updateRepositoryFromModel( groupRepository, configuration, group );
    }

    public GroupRepository updateRepositoryFromModel( GroupRepository old, Configuration configuration,
        CRepositoryGroup group )
        throws InvalidConfigurationException
    {
        GroupRepository repository = null;

        String type = group.getType();

        try
        {
            LocalRepositoryStorage ls = null;

            if ( group.getLocalStorage() != null )
            {
                ls = getLocalRepositoryStorage( group.getGroupId(), group.getLocalStorage().getProvider() );
            }
            else
            {
                ls = getLocalRepositoryStorage( group.getGroupId(), DEFAULT_LS_PROVIDER );
            }

            // Setting contentClass specific things on a repository
            GroupRepositoryConfigurator configurator = plexusContainer.lookup(
                GroupRepositoryConfigurator.class,
                type );

            repository = configurator.updateRepositoryFromModel( old, nexusConfiguration, group, ls );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Repository of type='" + type
                + "' does not have a valid configurator!", e );
        }

        return repository;
    }

    // ----------------------------------------
    // private stuff

    private <T extends Repository> T createRepository( Class<T> role, String hint )
        throws InvalidConfigurationException
    {
        try
        {
            return role.cast( plexusContainer.lookup( role, hint ) );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Could not lookup a new instance of Repository!", e );
        }
    }

    private LocalRepositoryStorage getLocalRepositoryStorage( String repoId, String provider )
        throws InvalidConfigurationException
    {
        try
        {
            return plexusContainer.lookup( LocalRepositoryStorage.class, provider );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Repository " + repoId
                + " have local storage with unsupported provider: " + provider, e );
        }
    }

    private RemoteRepositoryStorage getRemoteRepositoryStorage( String repoId, String provider )
        throws InvalidConfigurationException
    {
        try
        {
            return plexusContainer.lookup( RemoteRepositoryStorage.class, provider );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Repository " + repoId
                + " have remote storage with unsupported provider: " + provider, e );
        }
    }
}
