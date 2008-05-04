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

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class RepositoryRegistryEvent.
 */
public abstract class RepositoryRegistryEvent
    extends AbstractEvent
{

    /** The repository. */
    private Repository repository;

    /**
     * Instantiates a new repository registry event.
     * 
     * @param repository the repository
     */
    public RepositoryRegistryEvent( Repository repository )
    {
        super();
        this.repository = repository;
    }

    /**
     * Gets the repository.
     * 
     * @return the repository
     */
    public Repository getRepository()
    {
        return this.repository;
    }

}
