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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class RepositoryItemUid. This class represents unique and constant label of all items/files originating from a
 * Repository, thus backed by some storage (eg. Filesystem).
 */
public class RepositoryItemUid
{
    private static final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<String, ReentrantLock>();

    /** Constant to denote a separator in Proximity paths. */
    public static final String PATH_SEPARATOR = "/";

    /** Constant to represent a root of the path. */
    public static final String PATH_ROOT = PATH_SEPARATOR;

    /** The repository. */
    private Repository repository;

    /** The path. */
    private String path;

    /**
     * Instantiates a new repository item uid.
     * 
     * @param registry the registry
     * @param uidStr the uid str
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchRepositoryException the no such repository exception
     */
    public RepositoryItemUid( RepositoryRegistry registry, String uidStr )
        throws IllegalArgumentException,
            NoSuchRepositoryException
    {
        super();
        if ( uidStr.indexOf( ":" ) > -1 )
        {
            String[] parts = uidStr.split( ":" );
            if ( parts.length == 2 )
            {
                Repository repository = registry.getRepository( parts[0] );
                setUp( repository, parts[1] );
            }
            else
            {
                throw new IllegalArgumentException( uidStr
                    + " is malformed RepositoryItemUid! The proper format is '<repoId>:/path/to/something'." );
            }
        }
        else
        {
            throw new IllegalArgumentException( uidStr
                + " is malformed RepositoryItemUid! The proper format is '<repoId>:/path/to/something'." );
        }
    }

    /**
     * Instantiates a new repository item uid.
     * 
     * @param repository the repository
     * @param path the path
     */
    public RepositoryItemUid( Repository repository, String path )
    {
        super();
        setUp( repository, path );
    }

    /**
     * Sets the up.
     * 
     * @param repository the repository
     * @param path the path
     */
    private void setUp( Repository repository, String path )
    {
        this.repository = repository;
        if ( !StringUtils.isEmpty( path ) )
        {
            if ( path.startsWith( RepositoryItemUid.PATH_ROOT ) )
            {
                this.path = path;
            }
            else
            {
                this.path = RepositoryItemUid.PATH_ROOT + path;
            }
        }
        else
        {
            this.path = RepositoryItemUid.PATH_ROOT;
        }
    }

    /**
     * Gets the path.
     * 
     * @return the path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Gets the repository.
     * 
     * @return the repository
     */
    public Repository getRepository()
    {
        return repository;
    }

    public String toString()
    {
        return getRepository().getId() + ":" + getPath();
    }

    public static int getLockCount()
    {
        return locks.size();
    }

    public ReentrantLock lock()
    {
        ReentrantLock newLock = new ReentrantLock();

        ReentrantLock oldLock = locks.putIfAbsent( toString(), newLock );

        ReentrantLock lock = ( oldLock == null ) ? newLock : oldLock;

        lock.lock();

        return lock;
    }

    public void unlock( ReentrantLock lock )
    {
        synchronized ( locks )
        {
            if ( lock != null )
            {
                if ( !lock.hasQueuedThreads() )
                {
                    locks.remove( toString() );
                }

                lock.unlock();
            }
        }
    }
}
