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
package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

/**
 * The superclass of group related registry events.
 * 
 * @author cstamas
 */
public abstract class RepositoryRegistryGroupEvent
    extends RepositoryRegistryEvent
{
    /** The groupId */
    private final String groupId;

    /**
     * Instantiates a new repository registry event.
     * 
     * @param repository the repository
     */
    public RepositoryRegistryGroupEvent( final RepositoryRegistry repositoryRegistry, final String groupId )
    {
        super( repositoryRegistry );

        this.groupId = groupId;
    }

    public String getGroupId()
    {
        return groupId;
    }

}
