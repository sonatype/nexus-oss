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

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.InvalidGroupingException;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.templates.repository.maven.Maven2GroupRepositoryTemplate;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

public abstract class AbstractRepositoryGroupPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String GROUP_ID_KEY = "groupId";

    @Requirement
    private PlexusContainer container;

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

    @SuppressWarnings( "unchecked" )
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
            Maven2GroupRepositoryTemplate template =
                (Maven2GroupRepositoryTemplate) getNexus().getRepositoryTemplates()
                    .getTemplates( Maven2GroupRepositoryTemplate.class ).pick();

            template.getConfigurableRepository().setId( model.getId() );

            template.getConfigurableRepository().setName( model.getName() );

            template.getConfigurableRepository().setExposed( model.isExposed() );

            template.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

            for ( RepositoryGroupMemberRepository member : (List<RepositoryGroupMemberRepository>) model
                .getRepositories() )
            {
                template.getExternalConfiguration( true ).addMemberRepositoryId( member.getId() );
            }

            template.create();
        }
        // FIXME: cstamas or toby?
/*        catch ( NoSuchRepositoryAccessException e )
        {
            // access denied 403
            getLogger().warn( "Repository referenced by Repository Group Access Denied, ID=" + model.getId(), e );

            throw new PlexusResourceException(
                                               Status.CLIENT_ERROR_BAD_REQUEST,
                                               "Repository referenced by Repository Group Access Denied, GroupId="
                                                   + model.getId(),
                                               e,
                                               getNexusErrorResponse( "repositories",
                                                                      "Repository referenced by Repository Group Access Denied" ) );
        }
*/        catch ( InvalidGroupingException e )
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
