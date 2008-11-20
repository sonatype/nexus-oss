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
package org.sonatype.nexus.proxy;

/**
 * Thrown when a request is denied for security reasons. This exception should be ALWAYS considered as
 * "authorization denied" type of stuff, since Nexus does not deal with authentication. Simply taken, this exception is
 * thrown for lack of permissions of the already authenticated subject.
 * 
 * @author cstamas
 */
public class AccessDeniedException
    extends Exception
{
    private static final long serialVersionUID = 8341250956517740603L;

    private ResourceStoreRequest request;

    public AccessDeniedException( ResourceStoreRequest request, String msg )
    {
        super( msg );

        this.request = request;
    }

    /**
     * The RepositoryItemUid that is forbidden to access.
     * 
     * @return
     */
    public ResourceStoreRequest getResourceStoreRequest()
    {
        return this.request;
    }
}
