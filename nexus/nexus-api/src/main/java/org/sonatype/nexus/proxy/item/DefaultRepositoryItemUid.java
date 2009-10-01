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
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
    private static final ThreadLocal<Map<String, Lock>> threadCtx = new ThreadLocal<Map<String, Lock>>()
    {
        protected synchronized Map<String, Lock> initialValue()
        {
            return new HashMap<String, Lock>();
        };
    };

    private final RepositoryItemUidFactory factory;

    private final ReentrantReadWriteLock contentLock;

    private final ReentrantReadWriteLock attributesLock;

    /** The repository. */
    private final Repository repository;

    /** The path. */
    private final String path;

    protected DefaultRepositoryItemUid( RepositoryItemUidFactory factory, Repository repository, String path )
    {
        super();

        this.factory = factory;

        this.contentLock = new ReentrantReadWriteLock();

        this.attributesLock = new ReentrantReadWriteLock();

        this.repository = repository;

        this.path = path;
    }

    public RepositoryItemUidFactory getRepositoryItemUidFactory()
    {
        return factory;
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
        doLock( action, getLockKey(), contentLock );
    }

    public void unlock()
    {
        doUnlock( null, getLockKey(), contentLock );
    }

    public void lockAttributes( Action action )
    {
        doLock( action, getAttributeLockKey(), attributesLock );
    }

    public void unlockAttributes()
    {
        doUnlock( null, getAttributeLockKey(), attributesLock );
    }

    /**
     * toString() will return a "string representation" of this UID in form of repoId + ":" + path
     */
    public String toString()
    {
        return getRepository().getId() + ":" + getPath();
    }

    // ==

    protected Lock getLastUsedLock( String lockKey )
    {
        Map<String, Lock> threadMap = threadCtx.get();

        if ( !threadMap.containsKey( lockKey ) )
        {
            return null;
        }
        else
        {
            return threadMap.get( lockKey );
        }
    }

    protected void putLastUsedLock( String lockKey, Lock lock )
    {
        Map<String, Lock> threadMap = threadCtx.get();

        if ( lock != null )
        {
            threadMap.put( lockKey, lock );
        }
        else
        {
            threadMap.remove( lockKey );
        }
    }

    protected void doLock( Action action, String lockKey, ReentrantReadWriteLock rwLock )
    {
        Lock lock = getLastUsedLock( lockKey );

        if ( lock == null )
        {
            // get the needed lock and lock
            lock = getActionLock( rwLock, action );

            lock.lock();
        }
        else if ( lock != null && lock instanceof ReadLock && !action.isReadAction() )
        {
            // we need lock upgrade (r->w)
            lock.unlock();

            lock = getActionLock( rwLock, action );

            lock.lock();
        }
        else if ( lock != null && lock instanceof WriteLock && action.isReadAction() )
        {
            // we need lock downgrade (w->r)
            Lock wrLock = lock;

            lock = getActionLock( rwLock, action );

            lock.lock();

            wrLock.unlock();
        }

        putLastUsedLock( lockKey, lock );
    }

    public void doUnlock( Action action, String lockKey, ReentrantReadWriteLock rwLock )
    {
        Lock lock = getLastUsedLock( lockKey );

        // see the doLock: we lock only once! Hence, if the caller already has acquired lock, we do not lock it again,
        // simply not using the
        // "reentrant" nature of the locks, to keep this part simple as possible. Hence, the unlock may be called
        // multiple times (just as lock() is, but will do nothing).
        if ( lock != null )
        {
            lock.unlock();

            putLastUsedLock( lockKey, null );
        }
    }

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
