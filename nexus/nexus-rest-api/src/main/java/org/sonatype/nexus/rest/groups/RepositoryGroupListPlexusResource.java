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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupListResourceResponse;
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
@Path( RepositoryGroupListPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class RepositoryGroupListPlexusResource
    extends AbstractRepositoryGroupPlexusResource
{
    public static final String RESOURCE_URI = "/repo_groups";

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
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:repogroups]" );
    }

    /**
     * Get the list of repository groups defined in nexus.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = RepositoryGroupListResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryGroupListResourceResponse result = new RepositoryGroupListResourceResponse();

        Collection<GroupRepository> groups = getRepositoryRegistry().getRepositoriesWithFacet( GroupRepository.class );

        try
        {
            for ( GroupRepository group : groups )
            {
                RepositoryGroupListResource resource = new RepositoryGroupListResource();
                
                resource.setContentResourceURI( createRepositoryContentReference( request, group.getId() ).toString() );

                resource.setResourceURI( createRepositoryGroupReference( request, group.getId() ).toString() );

                resource.setId( group.getId() );

                resource.setExposed( group.isExposed() );

                resource.setFormat( getRepositoryRegistry()
                    .getRepositoryWithFacet( group.getId(), GroupRepository.class ).getRepositoryContentClass()
                    .getId() );

                resource.setName( group.getName() );

                result.addData( resource );
            }
        }
        catch ( NoSuchRepositoryAccessException e)
        {
            // access denied 403
            getLogger().debug( "Blocking access to all repository groups, based on permissions." );
            
            throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Cannot find a repository group or repository declared within a group!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }

        return result;
    }

    /**
     * Add a new repository group to nexus.
     */
    @Override
    @POST
    @ResourceMethodSignature( input = RepositoryGroupResourceResponse.class, output = RepositoryGroupResourceResponse.class )
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryGroupResourceResponse groupRequest = (RepositoryGroupResourceResponse) payload;

        if ( groupRequest != null )
        {
            RepositoryGroupResource resource = groupRequest.getData();

            createOrUpdateRepositoryGroup( resource, true );
            
            try
            {
                RepositoryGroupResourceResponse result = new RepositoryGroupResourceResponse();
                result.setData( buildGroupResource( request, resource.getId() ) );
                
                return result;
            }
            catch ( NoSuchRepositoryException e )
            {
                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "The group was somehow not found!",
                    getNexusErrorResponse( "repositories", "Group id not found!" ) );
            }
        }

        return null;
    }

}
