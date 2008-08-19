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
package org.sonatype.nexus.rest.wastebasket;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.WastebasketResource;
import org.sonatype.nexus.rest.model.WastebasketResourceResponse;
import org.sonatype.nexus.tasks.ClearCacheTask;
import org.sonatype.nexus.tasks.EmptyTrashTask;

/**
 * The Wastebasket resource handler. It returns the status of the wastebasket, and purges it.
 * 
 * @author cstamas
 */
public class WastebasketResourceHandler
    extends AbstractNexusResourceHandler
{
    /**
     * The resource constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public WastebasketResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * The default handler. It simply extracts the requested file name and gets the file's InputStream from Nexus
     * instance. If Nexus finds the file appropriate, the handler wraps it into InputStream representation and ships it
     * as "text/plain" media type, otherwise HTTP 404 is returned.
     */
    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        WastebasketResourceResponse response = new WastebasketResourceResponse();

        WastebasketResource resource = new WastebasketResource();

        resource.setItemCount( getNexus().getWastebasketItemCount() );

        resource.setSize( getNexus().getWastebasketSize() );

        response.setData( resource );

        return serialize( variant, response );
    }

    public boolean allowDelete()
    {
        return true;
    }

    public void delete()
    {
        EmptyTrashTask task = (EmptyTrashTask) getNexus().createTaskInstance( EmptyTrashTask.class );
        
        getNexus().submit( "Internal", task );
        
        getResponse().setStatus( Status.SUCCESS_NO_CONTENT );
    }
}
