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

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.sonatype.nexus.jsecurity.NoSuchEmailException;

public class UserForgotIdResourceHandler
    extends AbstractUserResourceHandler
{    
    private String email;
    
    public UserForgotIdResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
        
        this.email = getRequest().getAttributes().get( USER_EMAIL_KEY ).toString();
    }
    
    protected String getEmail()
    {
        return this.email;
    }
    
    @Override
    public boolean allowPost()
    {
        return true;
    }
    
    public void post( Representation representation )
    {
        try
        {
            getNexusSecurity().forgotUsername( getEmail() );
            
            getResponse().setStatus( Status.SUCCESS_ACCEPTED );
        }
        catch ( NoSuchEmailException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Email address not found!" );
            
            getLogger().log( Level.FINE, "Invalid email received: " + getEmail(), e );
        }
    }
}