/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.item;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class RepositoryItemUid. This class represents unique and constant label of all items/files originating from a
 * Repository, thus backed by some storage (eg. Filesystem).
 */
public class DefaultRepositoryItemUid
    implements RepositoryItemUid
{
    private static final ThreadLocal<Map<String, Object>> threadCtx = new ThreadLocal<Map<String, Object>>()
    {
        protected Map<String, Object> initialValue()
        {
            return new HashMap<String, Object>();
        };
    };

    private final RepositoryItemUidFactory factory;

    /** The repository. */
    private final Repository repository;

    /** The path. */
    private final String path;

    public DefaultRepositoryItemUid( RepositoryItemUidFactory factory, Repository repository, String path )
    {
        super();

        this.factory = factory;

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

    public void lock( Action action )
    {
        Lock lock = (Lock) threadCtx.get().get( getLockKey() );

        if ( lock == null )
        {
            // get the needed lock and lock
            lock = getActionLock( factory.acquireLock( this ), action );

            lock.lock();
        }
        else if ( lock != null && lock instanceof ReadLock && !action.isReadAction() )
        {
            synchronized ( lock )
            {
                // we need lock upgrade (r->w)
                lock.unlock();

                lock = getActionLock( factory.acquireLock( this ), action );

                lock.lock();
            }
        }
        else if ( lock != null && lock instanceof WriteLock && action.isReadAction() )
        {
            synchronized ( lock )
            {
                // we need lock downgrade (w->r)
                Lock wrLock = lock;

                lock = getActionLock( factory.acquireLock( this ), action );

                lock.lock();

                wrLock.unlock();
            }
        }

        threadCtx.get().put( getLockKey(), lock );
    }

    public void unlock()
    {
        Lock lock = (Lock) threadCtx.get().get( getLockKey() );

        if ( lock != null )
        {
            synchronized ( lock )
            {
                lock.unlock();

                threadCtx.get().remove( getLockKey() );

            }
        }

        factory.releaseLock( this );
    }

    public void lockAttributes( Action action )
    {
        Lock lock = (Lock) threadCtx.get().get( getAttributeLockKey() );

        if ( lock == null )
        {
            // get the needed lock and lock
            lock = getActionLock( factory.acquireAttributesLock( this ), action );

            lock.lock();
        }
        else if ( lock != null && lock instanceof ReadLock && !action.isReadAction() )
        {
            synchronized ( lock )
            {
                // we need lock upgrade (r->w)
                lock.unlock();

                lock = getActionLock( factory.acquireAttributesLock( this ), action );

                lock.lock();
            }
        }
        else if ( lock != null && lock instanceof WriteLock && action.isReadAction() )
        {
            synchronized ( lock )
            {
                // we need lock downgrade (w->r)
                Lock wrLock = lock;

                lock = getActionLock( factory.acquireAttributesLock( this ), action );

                lock.lock();

                wrLock.unlock();
            }
        }

        threadCtx.get().put( getAttributeLockKey(), lock );
    }

    public void unlockAttributes()
    {
        Lock lock = (Lock) threadCtx.get().get( getAttributeLockKey() );

        if ( lock != null )
        {
            synchronized ( lock )
            {
                lock.unlock();

                threadCtx.get().remove( getAttributeLockKey() );
            }
        }

        factory.releaseAttributesLock( this );
    }

    /**
     * toString() will return a "string representation" of this UID in form of repoId + ":" + path
     */
    public String toString()
    {
        return getRepository().getId() + ":" + getPath();
    }

    // ==

    private Lock getActionLock( ReadWriteLock rwLock, Action action )
    {
        if ( action.isReadAction() )
        {
            return rwLock.readLock();
        }
        else
        {
            return rwLock.writeLock();
        }
    }

    private String getLockKey()
    {
        return toString() + " : itemlock";
    }

    private String getAttributeLockKey()
    {
        return toString() + " : attrlock";
    }
}
