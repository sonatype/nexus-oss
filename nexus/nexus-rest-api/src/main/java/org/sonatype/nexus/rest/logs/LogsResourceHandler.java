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
import java.io.InputStream;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;

/**
 * The log file resource handler. It returns the content of the requested log file on incoming GET methods.
 * 
 * @author cstamas
 */
public class LogsResourceHandler
    extends AbstractNexusResourceHandler
{
    /** Key for retrieving the requested filename from request. */
    public static final String FILE_NAME_KEY = "fileName";

    /**
     * The resource constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public LogsResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        getVariants().clear();

        getVariants().add( new Variant( MediaType.TEXT_PLAIN ) );
    }

    /**
     * The default handler. It simply extracts the requested file name and gets the file's InputStream from Nexus
     * instance. If Nexus finds the file appropriate, the handler wraps it into InputStream representation and ships it
     * as "text/plain" media type, otherwise HTTP 404 is returned.
     */
    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        String logFile = getRequest().getAttributes().get( FILE_NAME_KEY ).toString();

        InputStream logFileStream = getNexus().getApplicationLogAsStream( logFile );

        if ( logFileStream != null )
        {
            return new InputStreamRepresentation( MediaType.TEXT_PLAIN, logFileStream );
        }
        else
        {
            getLogger().log( Level.WARNING, "Log file not found, filename=" + logFile );

            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Log file not found" );

            return null;
        }
    }
}
