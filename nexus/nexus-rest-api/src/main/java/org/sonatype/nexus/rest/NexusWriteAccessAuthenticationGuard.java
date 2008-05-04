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
package org.sonatype.nexus.rest;

import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class NexusWriteAccessAuthenticationGuard
    extends NexusAuthenticationGuard
{
    public NexusWriteAccessAuthenticationGuard( Context context )
    {
        super( context );
    }

    public NexusWriteAccessAuthenticationGuard( Context context, String usernameFilter )
    {
        super( context, usernameFilter );
    }

    public void doHandle( Request request, Response response )
    {
        if ( request.getMethod().equals( Method.GET ) || request.getMethod().equals( Method.HEAD )
            || request.getMethod().equals( Method.OPTIONS ) )
        {
            if ( getNext() != null )
            {
                getNext().handle( request, response );
            }
            else
            {
                response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
            }
        }
        else
        {
            super.doHandle( request, response );
        }
    }
}
