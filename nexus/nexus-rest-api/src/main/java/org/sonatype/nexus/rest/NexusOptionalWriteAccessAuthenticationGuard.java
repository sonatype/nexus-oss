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
import org.restlet.data.Request;
import org.sonatype.nexus.security.SimpleAuthenticationSource;

public class NexusOptionalWriteAccessAuthenticationGuard
    extends NexusWriteAccessAuthenticationGuard
{
    public NexusOptionalWriteAccessAuthenticationGuard( Context context, String usernameFilter )
    {
        super( context, usernameFilter );
    }

    public int authenticate( Request request )
    {
        if ( SimpleAuthenticationSource.class.isAssignableFrom( getAuthenticationSource().getClass() ) )
        {
            SimpleAuthenticationSource simple = (SimpleAuthenticationSource) getAuthenticationSource();

            // if the filtered username is known to authSource AND has password set
            if ( simple.hasPasswordSet( getUsernameFilter() ) )
            {
                return super.authenticate( request );
            }
            else
            {
                // fake it that we are authed
                return 1;
            }
        }
        else
        {
            return super.authenticate( request );
        }
    }
}
