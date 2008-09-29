/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.logs;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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

/**
 * The log file resource handler. It returns the content of the requested log file on incoming GET methods.
 * 
 * @author cstamas
 * @plexus.component role-hint="logs"
 */
public class LogsPlexusResource
    extends AbstractNexusPlexusResource
{
    /** Key for retrieving the requested filename from request. */
    public static final String FILE_NAME_KEY = "fileName";

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
        return "/logs/{" + FILE_NAME_KEY + "}";
    }

    /**
     * The default handler. It simply extracts the requested file name and gets the file's InputStream from Nexus
     * instance. If Nexus finds the file appropriate, the handler wraps it into InputStream representation and ships it
     * as "text/plain" media type, otherwise HTTP 404 is returned.
     */
    @Override
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
            result = getNexusInstance( request ).getApplicationLogAsStream( logFile, from, count );
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
