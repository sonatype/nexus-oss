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
package org.sonatype.nexus.rest.logs;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * The log file resource handler. It returns the content of the requested log file on incoming GET methods.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "logs" )
@Path( LogsPlexusResource.RESOURCE_URI )
@Produces( { "text/plain" } )
public class LogsPlexusResource
    extends AbstractNexusPlexusResource
{
    /** Key for retrieving the requested filename from request. */
    public static final String FILE_NAME_KEY = "fileName";
    
    public static final String RESOURCE_URI = "/logs/{" + FILE_NAME_KEY + "}"; 

    @Override
    public List<Variant> getVariants()
    {
        return Collections.singletonList( new Variant( MediaType.TEXT_PLAIN ) );
    }

    @Override
    public Object getPayloadInstance()
    {
        // TODO Auto-generated method stub
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
        return new PathProtectionDescriptor( "/logs/*", "authcBasic,perms[nexus:logs]" );
    }

    /**
     * The default handler. It simply extracts the requested file name and gets the file's InputStream from Nexus
     * instance. If Nexus finds the file appropriate, the handler wraps it into InputStream representation and ships it
     * as "text/plain" media type, otherwise HTTP 404 is returned.
     * 
     * @param fileName The file name to retrieve (as defined in the log list resource response).
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( LogsPlexusResource.FILE_NAME_KEY ) }, output = Object.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String logFile = request.getAttributes().get( FILE_NAME_KEY ).toString();

        Form params = request.getResourceRef().getQueryAsForm();

        String fromStr = params.getFirstValue( "from" );

        String countStr = params.getFirstValue( "count" );

        long from = 0;

        long count = Long.MAX_VALUE;

        if ( !StringUtils.isEmpty( fromStr ) )
        {
            from = Long.valueOf( fromStr );
        }

        if ( !StringUtils.isEmpty( countStr ) )
        {
            count = Long.valueOf( countStr );
        }

        NexusStreamResponse result;
        try
        {
            result = getNexus().getApplicationLogAsStream( logFile, from, count );
        }
        catch ( IOException e )
        {
            throw new ResourceException( e );
        }

        if ( result != null )
        {
            return new InputStreamRepresentation( MediaType.valueOf( result.getMimeType() ), result.getInputStream() );
        }
        else
        {
            getLogger().warn( "Log file not found, filename=" + logFile );

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Log file not found" );
        }
    }
}
