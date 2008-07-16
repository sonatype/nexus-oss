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
package org.sonatype.nexus.rest.privileges;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.PrivilegeApplicationStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeListResourceResponse;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;

public class PrivilegeListResourceHandler
extends AbstractPrivilegeResourceHandler
{

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public PrivilegeListResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * We are handling HTTP GETs/
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * We create the List of Repositories by getting the from Nexus App.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        PrivilegeListResourceResponse response = new PrivilegeListResourceResponse();

        //TODO: Retrieve items from Nexus, currently just hardcoded junk
        //TODO: will also be able to call the nexusToRestModel method in parent class
        PrivilegeBaseStatusResource resource = new PrivilegeApplicationStatusResource();
        resource.setId( "globalSettings-read" );
        resource.setName( "Global Settings - Read" );
        resource.setResourceUri( calculateSubReference( resource.getId() ).toString() );
        resource.setMethod( "read" );
        resource.setType( AbstractPrivilegeResourceHandler.TYPE_APPLICATION );
        ( ( PrivilegeApplicationStatusResource ) resource ).setPath( "/service/local/global_settings" );
        
        response.addData( resource );
        
        resource = new PrivilegeApplicationStatusResource();
        resource.setId( "globalSettings-write" );
        resource.setName( "Global Settings - Write" );
        resource.setResourceUri( calculateSubReference( resource.getId() ).toString() );
        resource.setMethod( "write" );
        resource.setType( AbstractPrivilegeResourceHandler.TYPE_APPLICATION );
        ( ( PrivilegeApplicationStatusResource ) resource ).setPath( "/service/local/global_settings" );
        
        response.addData( resource );
        
        resource = new PrivilegeApplicationStatusResource();
        resource.setId( "globalSettings-update" );
        resource.setName( "Global Settings - Update" );
        resource.setResourceUri( calculateSubReference( resource.getId() ).toString() );
        resource.setMethod( "update" );
        resource.setType( AbstractPrivilegeResourceHandler.TYPE_APPLICATION );
        ( ( PrivilegeApplicationStatusResource ) resource ).setPath( "/service/local/global_settings" );
        
        response.addData( resource );
        
        resource = new PrivilegeApplicationStatusResource();
        resource.setId( "globalSettings-delete" );
        resource.setName( "Global Settings - Delete" );
        resource.setResourceUri( calculateSubReference( resource.getId() ).toString() );
        resource.setMethod( "delete" );
        resource.setType( AbstractPrivilegeResourceHandler.TYPE_APPLICATION );
        ( ( PrivilegeApplicationStatusResource ) resource ).setPath( "/service/local/global_settings" );
        
        response.addData( resource );
                
        return serialize( variant, response );
    }

    /**
     * This resource allows PUT.
     */
    public boolean allowPost()
    {
        return true;
    }

    public void post( Representation representation )
    {
        PrivilegeResourceRequest request = (PrivilegeResourceRequest) deserialize( new PrivilegeResourceRequest() );

        if ( request == null )
        {
            return;
        }
        else
        {
            PrivilegeBaseResource resource = request.getData();
            
            if ( validateFields( resource, representation ) )
            {
                //TODO: actually store the data here
            }
        }
    }
}
