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
package org.sonatype.nexus.rest.status;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.SystemState;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

@Component( role = ManagedPlexusResource.class, hint = "CommandPlexusResource" )
@Path( CommandPlexusResource.RESOURCE_URI )
@Consumes( { "application/xml", "application/json" } )
public class CommandPlexusResource
    extends AbstractNexusPlexusResource
    implements ManagedPlexusResource
{
    public static final String RESOURCE_URI = "/status/command"; 

    public CommandPlexusResource()
    {
        this.setReadable( false );
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new String();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:command]" );
    }

    /**
     * Control the nexus server, you can START, STOP, RESTART or KILL it.
     */
    @Override
    @PUT
    @ResourceMethodSignature( input = String.class )
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        boolean result = false;

        try
        {
            String cmd = (String) payload;

            if ( "START".equalsIgnoreCase( cmd ) )
            {
                result = getNexus().setState( SystemState.STARTED );
            }
            else if ( "STOP".equalsIgnoreCase( cmd ) )
            {
                result = getNexus().setState( SystemState.STOPPED );
            }
            else if ( "RESTART".equalsIgnoreCase( cmd ) )
            {
                // if running stop it
                if ( SystemState.STARTED.equals( getNexus().getSystemStatus().getState() ) )
                {
                    getNexus().setState( SystemState.STOPPED );
                }

                // and start it
                result = getNexus().setState( SystemState.STARTED );
            }
            else if ( "KILL".equalsIgnoreCase( cmd ) )
            {
                // if running stop it
                if ( SystemState.STARTED.equals( getNexus().getSystemStatus().getState() ) )
                {
                    getNexus().setState( SystemState.STOPPED );
                }

                System.exit( 0 );
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Unknown COMMAND!" );
            }

            if ( result )
            {
                response.setStatus( Status.SUCCESS_NO_CONTENT );
            }
            else
            {
                throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Could not change Nexus state to submitted one! (check logs for more info)" );
            }
        }
        catch ( IllegalArgumentException e )
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "Could not change Nexus state to submitted one! (check logs for more info)" );
        }
        // status is 204
        return null;
    }

}
