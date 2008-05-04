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
import java.util.Collection;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;

/**
 * The log file list resource handler. This handles the GET method only and simply returns the list of existing nexus
 * application log files.
 * 
 * @author cstamas
 */
public class LogsListResourceHandler
    extends AbstractNexusResourceHandler
{

    /**
     * The resource constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public LogsListResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * The default handler. It simply gets the list of log files from Nexus instance and wraps them into REST DTO.
     */
    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        Collection<String> logFiles = getNexus().getApplicationLogFiles();

        LogsListResourceResponse response = new LogsListResourceResponse();

        for ( String fileName : logFiles )
        {
            LogsListResource resource = new LogsListResource();

            resource.setResourceURI( calculateSubReference( fileName ).getPath() );

            resource.setName( fileName );

            response.addData( resource );
        }

        return serialize( variant, response );
    }
}
