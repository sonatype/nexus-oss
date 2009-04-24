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
import java.util.List;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

import com.thoughtworks.xstream.io.xml.xppdom.Xpp3Dom;

public abstract class AbstractRepositoryGroupPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String GROUP_ID_KEY = "groupId";

    @SuppressWarnings( "unchecked" )
    protected void createOrUpdateRepositoryGroup( RepositoryGroupResource model, boolean create )
        throws ResourceException
    {
        try
        {
            GroupRepository group = null;

            if ( create )
            {
                group = new CRepositoryGroup();

                group.setGroupId( model.getId() );
            }
            else
            {
                group = getNexus().readRepositoryGroup( model.getId() );
            }

            group.setName( model.getName() );

            group.setType( model.getProvider() );

            group.getRepositories().clear();

            for ( RepositoryGroupMemberRepository member : (List<RepositoryGroupMemberRepository>) model.getRepositories() )
            {
                group.addRepository( member.getId() );
            }

            if ( create )
            {
                getNexus().createRepositoryGroup( group );
            }
            else
            {
                getNexus().updateRepositoryGroup( group );
            }
        }
        catch ( ConfigurationException e )
        {
            handleConfigurationException( e );
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
                                                                      "Repository referenced by Repository Group does not share same content type!" ) );
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
            CRepository group = new CRepository();
            
            group.setName( model.getName() );
            
            group.setProviderRole( GroupRepository.class.getName() );

            group.setProviderHint( model.getProvider() );
            
            group.getExternalConfiguration()
            
            M2GroupRepositoryConfiguration conf = new M2GroupRepositoryConfiguration();

            group.getM.getRepositories().clear();

            for ( RepositoryGroupMemberRepository member : (List<RepositoryGroupMemberRepository>) model.getRepositories() )
            {
                group.addRepository( member.getId() );
            }

            if ( create )
            {
                getNexus().createRepositoryGroup( group );
            }
            else
            {
                getNexus().updateRepositoryGroup( group );
            }
        }
        catch ( ConfigurationException e )
        {
            handleConfigurationException( e );
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
                                                                      "Repository referenced by Repository Group does not share same content type!" ) );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Got IO Exception!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
    }
}
