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

import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

/**
 * A resource list for RepositoryGroup list.
 * 
 * @author cstamas
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryGroupListPlexusResource" )
public class RepositoryGroupListPlexusResource
    extends AbstractRepositoryGroupPlexusResource
{

    public RepositoryGroupListPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryGroupResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/repo_groups";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:repogroups]" );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryGroupListResourceResponse result = new RepositoryGroupListResourceResponse();

        Collection<CRepositoryGroup> groups = getNexus().listRepositoryGroups();

        try
        {
            for ( CRepositoryGroup group : groups )
            {
                RepositoryGroupListResource resource = new RepositoryGroupListResource();

                resource.setResourceURI( createRepositoryGroupReference( request, group.getGroupId() ).toString() );

                resource.setId( group.getGroupId() );

                resource.setFormat( getNexus()
                    .getRepositoryWithFacet( group.getGroupId(), GroupRepository.class ).getRepositoryContentClass()
                    .getId() );

                resource.setName( group.getName() );

                // just to trigger list creation, and not stay null coz of XStream serialization
                resource.getRepositories();

                for ( String repoId : (List<String>) group.getRepositories() )
                {
                    RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();

                    member.setId( repoId );

                    member.setName( getNexus().getRepository( repoId ).getName() );

                    member.setResourceURI( createRepositoryReference( request, repoId ).toString() );

                    resource.addRepository( member );

                }

                result.addData( resource );

            }
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Cannot find a repository group or repository declared within a group!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }

        return result;
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryGroupResourceResponse groupRequest = (RepositoryGroupResourceResponse) payload;

        if ( groupRequest != null )
        {
            RepositoryGroupResource resource = groupRequest.getData();

            if ( resource.getRepositories() == null || resource.getRepositories().size() == 0 )
            {
                getLogger()
                    .info( "The repository group with ID=" + resource.getId() + " have zero repository members!" );

                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "The group cannot have zero repository members!",
                    getNexusErrorResponse( "repositories", "The group cannot have zero repository members!" ) );
            }

            createOrUpdateRepositoryGroup( resource, true );
        }

        // TODO: return the group
        return null;
    }

}
