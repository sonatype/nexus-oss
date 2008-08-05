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
package org.sonatype.nexus.scheduling;

public abstract class AbstractNexusRepositoriesPathAwareTask<T>
    extends AbstractNexusRepositoriesTask<T>
{
    public static final String RESOURCE_STORE_PATH_KEY = "resourceStorePath";

    public String getResourceStorePath()
    {
        return getParameters().get( RESOURCE_STORE_PATH_KEY );
    }

    public void setResourceStorePath( String resourceStorePath )
    {
        getParameters().put( RESOURCE_STORE_PATH_KEY, resourceStorePath );
    }
}
