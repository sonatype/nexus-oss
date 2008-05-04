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
package org.sonatype.nexus.security;

/**
 * Simple user.
 * 
 * @author cstamas
 */
public class SimpleUser
    implements User
{
    private static final String ANONYMOUS = "anonymous";

    public static final User ANONYMOUS_USER = new SimpleUser( ANONYMOUS );

    private String username;

    public SimpleUser( String username )
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

    public String getEmail()
    {
        return null;
    }

    public boolean isAnonymous()
    {
        return ANONYMOUS.equals( getUsername() );
    }
}
