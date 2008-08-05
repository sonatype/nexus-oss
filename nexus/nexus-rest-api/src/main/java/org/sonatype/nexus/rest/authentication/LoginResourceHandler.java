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
package org.sonatype.nexus.rest.authentication;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.AuthenticationLoginResource;
import org.sonatype.nexus.rest.model.AuthenticationLoginResourceResponse;

/**
 * The login resource handler. It creates a user token.
 * 
 * @author cstamas
 */
public class LoginResourceHandler
    extends AbstractUIPermissionCalculatingResource
{
    public LoginResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        AuthenticationLoginResource resource = new AuthenticationLoginResource();

        resource.setClientPermissions( getClientPermissionsForCurrentUser() );

        AuthenticationLoginResourceResponse response = new AuthenticationLoginResourceResponse();

        response.setData( resource );

        return serialize( variant, response );
    }

}
