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
package org.sonatype.nexus.proxy.item;

import java.util.concurrent.locks.ReentrantLock;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class RepositoryItemUid. This class represents unique and constant label of all items/files originating from a
 * Repository, thus backed by some storage (eg. Filesystem).
 */
public class DefaultRepositoryItemUid
    implements RepositoryItemUid
{
    /** The lock used for locking UID */
    private final ReentrantLock lock;

    /** The repository. */
    private final Repository repository;

    /** The path. */
    private final String path;

    protected DefaultRepositoryItemUid( ReentrantLock lock, Repository repository, String path )
    {
        super();

        this.lock = lock;

        this.repository = repository;

        this.path = path;
    }

    public Repository getRepository()
    {
        return repository;
    }

    public String getPath()
    {
        return path;
    }

    public void lock()
    {
        if ( lock != null )
        {
            lock.lock();
        }
    }

    public void unlock()
    {
        if ( lock != null )
        {
            lock.unlock();
        }
    }

    public ReentrantLock getLock()
    {
        return lock;
    }

    public int getQueueLength()
    {
        return lock.getQueueLength();
    }

    /**
     * toString() will return a "string representation" of this UID in form of repoId + ":" + path
     */
    public String toString()
    {
        return getRepository().getId() + ":" + getPath();
    }

}
