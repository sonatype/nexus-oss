/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.wastebasket;

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
import org.sonatype.nexus.proxy.statistics.DeferredLong;
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
        WastebasketResourceResponse result = new WastebasketResourceResponse();

        WastebasketResource resource = new WastebasketResource();

        resource.setItemCount( -1 );

        DeferredLong totalSize = wastebasket.getTotalSize();

        if ( totalSize.isDone() )
        {
            resource.setSize( wastebasket.getTotalSize().getValue() );
        }
        else
        {
            resource.setSize( -1 );
        }

        result.setData( resource );

        return result;
    }

    /**
     * Empty the wastebasket.
     */
    @Override
    @DELETE
    @ResourceMethodSignature( )
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
