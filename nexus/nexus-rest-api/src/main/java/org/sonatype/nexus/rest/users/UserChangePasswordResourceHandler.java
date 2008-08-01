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
package org.sonatype.nexus.rest.users;

import java.io.IOException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.sonatype.nexus.configuration.security.InvalidCredentialsException;
import org.sonatype.nexus.configuration.security.NoSuchUserException;
import org.sonatype.nexus.rest.model.UserChangePasswordRequest;
import org.sonatype.nexus.rest.model.UserChangePasswordResource;

public class UserChangePasswordResourceHandler
    extends AbstractUserResourceHandler
{
    public UserChangePasswordResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }
    
    @Override
    public boolean allowPost()
    {
        return true;
    }
    
    @Override
    public void post( Representation representation )
    {
        UserChangePasswordRequest request = (UserChangePasswordRequest) deserialize( new UserChangePasswordRequest() );

        if ( request == null )
        {
            return;
        }
        else
        {
            UserChangePasswordResource resource = request.getData();
            
            try
            {
                getNexusSecurityConfiguration().changePassword( resource.getUserId(), resource.getOldPassword(), resource.getNewPassword() );
            }
            catch ( IOException e )
            {
                getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

                getLogger().log( Level.SEVERE, "Got IO Exception!", e );
            }
            catch ( NoSuchUserException e )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid credentials supplied." );

                getLogger().log( Level.FINE, "Invalid user ID!", e );
            }
            catch ( InvalidCredentialsException e )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid credentials supplied." );

                getLogger().log( Level.FINE, "Invalid credentials!", e );
            }
        }
    }
}