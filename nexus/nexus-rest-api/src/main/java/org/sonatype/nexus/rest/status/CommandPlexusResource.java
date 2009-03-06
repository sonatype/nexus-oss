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
package org.sonatype.nexus.rest.status;

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
public class CommandPlexusResource
    extends AbstractNexusPlexusResource
    implements ManagedPlexusResource
{

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
        return "/status/command";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:command]" );
    }

    @Override
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
