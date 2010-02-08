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
package org.sonatype.nexus.rest.wastebasket;

import java.io.IOException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.WastebasketResource;
import org.sonatype.nexus.rest.model.WastebasketResourceResponse;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.EmptyTrashTask;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * The Wastebasket resource handler. It returns the status of the wastebasket, and purges it.
 * 
 * @author cstamas
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "wastebasket" )
@Path( WastebasketPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class WastebasketPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String RESOURCE_URI = "/wastebasket";
    
    @Requirement
    private Wastebasket wastebasket;
    
    @Requirement
    private NexusScheduler nexusScheduler;
    
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/wastebasket**", "authcBasic,perms[nexus:wastebasket]" );
    }

    /**
     * Get details about the contents of the wastebasket.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = WastebasketResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        try
        {
            WastebasketResourceResponse result = new WastebasketResourceResponse();

            WastebasketResource resource = new WastebasketResource();

            resource.setItemCount( wastebasket.getItemCount() );

            resource.setSize( wastebasket.getSize() );

            result.setData( resource );

            return result;

        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "IOException during configuration retrieval!", e );
        }
    }

    /**
     * Empty the wastebasket.
     */
    @Override
    @DELETE
    @ResourceMethodSignature()
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        EmptyTrashTask task = nexusScheduler.createTaskInstance( EmptyTrashTask.class );

        nexusScheduler.submit( "Internal", task );

        response.setStatus( Status.SUCCESS_NO_CONTENT );
    }

    @Override
    public boolean isModifiable()
    {
        return true;
    }

}
