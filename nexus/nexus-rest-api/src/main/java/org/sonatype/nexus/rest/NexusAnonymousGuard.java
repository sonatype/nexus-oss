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
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.sonatype.nexus.security.SimpleUser;

/**
 * A guard that simply puts Anonymous user into request attributes. This is needed when Nexus security is turned ON and
 * anonymous access is enabled. When security is shut down, it is simply neglected, but does not bother anything.
 * 
 * @author cstamas
 */
public class NexusAnonymousGuard
    extends Guard
{

    public NexusAnonymousGuard( Context context )
    {
        super( context, ChallengeScheme.CUSTOM, "Nexus REST API" );
    }

    public int authenticate( Request request )
    {
        request.getAttributes().put( NexusAuthenticationGuard.REST_USER_KEY, SimpleUser.ANONYMOUS_USER );

        // see the superclass. we are actually NOT protecting this resource,
        // only injecting ANONYMOUS_USER user for later in content handling.
        return 1;
    }

}
