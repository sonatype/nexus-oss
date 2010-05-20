/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.groups;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.InvalidGroupingException;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.NexusCompat;
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.repositories.AbstractRepositoryPlexusResource;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

public abstract class AbstractRepositoryGroupPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String GROUP_ID_KEY = "groupId";

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;
    
    protected RepositoryGroupResource buildGroupResource( Request request, String groupId ) 
        throws NoSuchRepositoryException,
            ResourceException
    {
        Repository repo = getRepositoryRegistry().getRepository( groupId );
        
        if ( repo.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            return buildGroupResource( request, repo.adaptToFacet( GroupRepository.class ) );
        }
        
        return null;
    }
    
    protected RepositoryGroupResource buildGroupResource( Request request, GroupRepository group )
        throws ResourceException
    {
        RepositoryGroupResource resource = new RepositoryGroupResource();
        
        resource.setContentResourceURI( createRepositoryContentReference( request, group.getId() ).toString() );
        
        resource.setId( group.getId() );

        resource.setName( group.getName() );

        resource.setProvider( NexusCompat.getRepositoryProviderHint( group ) );

        resource.setRepoType( AbstractRepositoryPlexusResource.REPO_TYPE_GROUP );

        resource.setFormat( group.getRepositoryContentClass().getId() );
        
        resource.setExposed( group.isExposed() );

        // just to trigger list creation, and not stay null coz of XStream serialization
        resource.getRepositories();

        for ( String repoId : group.getMemberRepositoryIds() )
        {
            RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();

            member.setId( repoId );

            try
            {
                // NOTE: we must hit the registry each time and NOT call groupRepo.getMemberRepositories, that doesn't block access
                member.setName( getRepositoryRegistry().getRepository( repoId ).getName() );
            }
            catch ( NoSuchRepositoryAccessException e)
            {
                // access denied 403
                getLogger().debug( "Blocking access to repository group, based on permissions." );
                
                throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN );
            }
            catch ( NoSuchRepositoryException e )
            {
                getLogger().debug( "Found missing repo id: " + repoId + " contained in group" );
            }            

            member.setResourceURI( createChildReference( request, this, repoId ).toString() );

            resource.addRepository( member );
        }
        
        return resource;
    }

    protected void createOrUpdateRepositoryGroup( RepositoryGroupResource model, boolean create )
        throws ResourceException
    {
        if ( create )
        {
            createRepositoryGroup( model );
        }
        else
        {
            updateRepositoryGroup( model );
        }
    }

    protected void updateRepositoryGroup( RepositoryGroupResource model )
        throws ResourceException
    {
        try
        {
            GroupRepository group =
                getRepositoryRegistry().getRepositoryWithFacet( model.getId(), GroupRepository.class );

            group.setName( model.getName() );

            group.setExposed( model.isExposed() );

            ArrayList<String> members = new ArrayList<String>();

            for ( RepositoryGroupMemberRepository member : (List<RepositoryGroupMemberRepository>) model
                .getRepositories() )
            {
                members.add( member.getId() );
            }

            group.setMemberRepositoryIds( members );

            getNexusConfiguration().saveConfiguration();
        }
        catch ( NoSuchRepositoryAccessException e )
        {
            // access denied 403
            getLogger().warn( "Repository referenced by Repository Group Access Eenied, ID=" + model.getId(), e );

            throw new PlexusResourceException(
                                               Status.CLIENT_ERROR_BAD_REQUEST,
                                               "Repository referenced by Repository Group Access Denied, GroupId="
                                                   + model.getId(),
                                               e,
                                               getNexusErrorResponse( "repositories",
                                                                      "Repository referenced by Repository Group Access Denied" ) );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Repository referenced by Repository Group Not Found, ID=" + model.getId(), e );

            throw new PlexusResourceException(
                                               Status.CLIENT_ERROR_BAD_REQUEST,
                                               "Repository referenced by Repository Group Not Found, GroupId="
                                                   + model.getId(),
                                               e,
                                               getNexusErrorResponse( "repositories",
                                                                      "Repository referenced by Repository Group Not Found" ) );
        }
        catch ( InvalidGroupingException e )
        {
            getLogger().warn( "Invalid grouping detected!, GroupId=" + model.getId(), e );

            throw new PlexusResourceException(
                                               Status.CLIENT_ERROR_BAD_REQUEST,
                                               "Invalid grouping requested, GroupId=" + model.getId(),
                                               e,
                                               getNexusErrorResponse( "repositories",
                                                                      e.getMessage() ) );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Got IO Exception!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
    }

    protected void createRepositoryGroup( RepositoryGroupResource model )
        throws ResourceException
    {
        try
        {
            ContentClass contentClass =
                repositoryTypeRegistry.getRepositoryContentClass( GroupRepository.class.getName(), model.getProvider() );

            RepositoryTemplate template =
                (RepositoryTemplate) getNexus().getRepositoryTemplates().getTemplates( GroupRepository.class,
                                                                                       contentClass ).pick();

            template.getConfigurableRepository().setId( model.getId() );

            template.getConfigurableRepository().setName( model.getName() );

            template.getConfigurableRepository().setExposed( model.isExposed() );

            template.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

            // we create an empty group
            GroupRepository groupRepository = (GroupRepository) template.create();

            ArrayList<String> memberIds = new ArrayList<String>( model.getRepositories().size() );

            for ( RepositoryGroupMemberRepository member : (List<RepositoryGroupMemberRepository>) model
                .getRepositories() )
            {
                memberIds.add( member.getId() );
            }

            groupRepository.setMemberRepositoryIds( memberIds );
            
            getNexusConfiguration().saveConfiguration();
        }
        // FIXME: cstamas or toby?
        /*
         * catch ( NoSuchRepositoryAccessException e ) { // access denied 403 getLogger().warn(
         * "Repository referenced by Repository Group Access Denied, ID=" + model.getId(), e ); throw new
         * PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
         * "Repository referenced by Repository Group Access Denied, GroupId=" + model.getId(), e,
         * getNexusErrorResponse( "repositories", "Repository referenced by Repository Group Access Denied" ) ); }
         */
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Repository referenced by group does not exists!, GroupId=" + model.getId(), e );

            throw new PlexusResourceException(
                                               Status.CLIENT_ERROR_BAD_REQUEST,
                                               "Repository referenced by group does not exists, GroupId="
                                                   + model.getId(),
                                               e,
                                               getNexusErrorResponse( "repositories",
                                                                      "Repository referenced by Repository Group does not exists!" ) );
        }
        catch ( InvalidGroupingException e )
        {
            getLogger().warn( "Invalid grouping detected!, GroupId=" + model.getId(), e );

            throw new PlexusResourceException(
                                               Status.CLIENT_ERROR_BAD_REQUEST,
                                               "Invalid grouping requested, GroupId=" + model.getId(),
                                               e,
                                               getNexusErrorResponse( "repositories",
                                                                      "Repository referenced by Repository Group does not share same content type!" ) );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Got IO Exception!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
        catch ( ConfigurationException e )
        {
            handleConfigurationException( e );
        }
    }
}
