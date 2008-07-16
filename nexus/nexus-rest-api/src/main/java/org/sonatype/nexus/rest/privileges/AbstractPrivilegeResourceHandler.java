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
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.PrivilegeApplicationStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;

public class AbstractPrivilegeResourceHandler
extends AbstractNexusResourceHandler
{
    public static final String PRIVILEGE_ID_KEY = "privilegeId";
    
    public static final String TYPE_APPLICATION = "application";
    public static final String TYPE_REPO_TARGET = "repositoryTarget";

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AbstractPrivilegeResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }
    
    public boolean validateFields( PrivilegeBaseResource resource, Representation representation )
    {
        //TODO: validation
        return true;
    }
    
    public PrivilegeBaseStatusResource nexusToRestModel()
    {
        //TODO: ultimately this method will take a parameter which is the nexus object
        //and will convert to the rest object
        PrivilegeBaseStatusResource resource = new PrivilegeApplicationStatusResource();
        resource.setId( "privid" );
        resource.setName( "privname" );
        resource.setResourceUri( calculateSubReference( resource.getId() ).toString() );
        resource.setMethod( "read" );
        resource.setType( AbstractPrivilegeResourceHandler.TYPE_APPLICATION );
        
        if ( resource.getClass().isAssignableFrom( PrivilegeApplicationStatusResource.class ) )
        {
            ( ( PrivilegeApplicationStatusResource ) resource ).setPath( "/service/local/global_settings" );
        }
                
        return resource;
    }
}
