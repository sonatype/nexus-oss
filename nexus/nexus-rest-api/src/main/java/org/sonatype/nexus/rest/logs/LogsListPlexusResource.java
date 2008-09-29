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
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;

/**
 * The log file list resource handler. This handles the GET method only and simply returns the list of existing nexus
 * application log files.
 * 
 * @author cstamas
 * 
 * @plexus.component role-hint="logsList"
 */
public class LogsListPlexusResource
    extends AbstractNexusPlexusResource
{
    @Override
    public Object getPayloadInstance()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String getResourceUri()
    {
        return "/logs";
    }
    
    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        LogsListResourceResponse result = new LogsListResourceResponse();
        
        try
        {
            Collection<NexusStreamResponse> logFiles = getNexusInstance( request ).getApplicationLogFiles();
    
            for ( NexusStreamResponse logFile : logFiles )
            {
                LogsListResource resource = new LogsListResource();
    
                resource.setResourceURI( createChildReference( request, logFile.getName() ).toString() );
    
                resource.setName( logFile.getName() );
                
                resource.setSize( logFile.getSize() );
                
                resource.setMimeType( logFile.getMimeType() );
    
                result.addData( resource );
            }
        }
        catch( IOException e )
        {
            throw new ResourceException( e );
        }

        return result;
    }
}
