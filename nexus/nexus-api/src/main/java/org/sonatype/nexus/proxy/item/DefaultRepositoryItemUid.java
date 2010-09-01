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

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class RepositoryItemUid. This class represents unique and constant label of all items/files originating from a
 * Repository, thus backed by some storage (eg. Filesystem).
 */
public class DefaultRepositoryItemUid
    implements RepositoryItemUid
{
    private static enum LockStep
    {
        READ, WRITE;

        public boolean isReadLockLastLocked()
        {
            return READ.equals( this );
        }
    }

    private static final ThreadLocal<Map<String, Stack<LockStep>>> threadCtx =
        new ThreadLocal<Map<String, Stack<LockStep>>>()
        {
            @Override
            protected synchronized Map<String, Stack<LockStep>> initialValue()
            {
                return new HashMap<String, Stack<LockStep>>();
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
    @Override
    public String toString()
    {
        return getRepository().getId() + ":" + getPath();
    }

    public String toDebugString()
    {
        return getRepository().getId() + ":" + getPath() + " (" + super.toString() + ")";
    }

    // ==

    protected Stack<LockStep> getPreviousSteps( String lockKey )
    {
        Map<String, Stack<LockStep>> threadMap = threadCtx.get();

        if ( !threadMap.containsKey( lockKey ) )
        {
            threadMap.put( lockKey, new Stack<LockStep>() );
        }

        return threadMap.get( lockKey );
    }

    protected LockStep getLastStep( String lockKey )
    {
        Stack<LockStep> steps = getPreviousSteps( lockKey );
        if ( steps.isEmpty() )
        {
            return null;
        }

        try
        {
            return steps.peek();
        }
        catch ( EmptyStackException e )
        {
            return null;
        }
    }

    protected void putLastStep( String lockKey, LockStep lock )
    {
        Stack<LockStep> steps = getPreviousSteps( lockKey );

        if ( lock != null )
        {
            steps.push( lock );
        }
        else
        {
            steps.pop();

            // cleanup if stack is empty
            if ( steps.isEmpty() )
            {
                Map<String, Stack<LockStep>> threadMap = threadCtx.get();
                threadMap.remove( lockKey );
            }
        }
    }

    protected void doLock( Action action, String lockKey, ReentrantReadWriteLock rwLock )
    {
        // if a write lock, needs to release all read locks first
        if ( !action.isReadAction() )
        {
            boolean needToReleaseRead = true;
            Stack<LockStep> steps = getPreviousSteps( lockKey );
            for ( LockStep step : steps )
            {
                if ( !step.isReadLockLastLocked() )
                {
                    needToReleaseRead = false;
                    break;
                }
            }

            if ( needToReleaseRead )
            {
                for ( LockStep lockStep : steps )
                {
                    getActionLock( rwLock, true ).unlock();
                }
            }
        }

        // lock it according to the action
        getActionLock( rwLock, action.isReadAction() ).lock();

        LockStep step = action.isReadAction() ? LockStep.READ : LockStep.WRITE;

        putLastStep( lockKey, step );
    }

    protected void doUnlock( Action action, String lockKey, ReentrantReadWriteLock rwLock )
    {
        LockStep step = getLastStep( lockKey );

        if ( step == null )
        {
            // this is error here
            throw new IllegalMonitorStateException( "UID \"" + toString()
                + "\" was tried to be unlocked but had no step-history..." );
        }

        switch ( step )
        {
            case READ:
                getActionLock( rwLock, true ).unlock();
                break;
            case WRITE:
                getActionLock( rwLock, false ).unlock();
                break;
        }

        putLastStep( lockKey, null );

        // if a write lock is released, it does need to redo all read locks
        if ( !step.isReadLockLastLocked() )
        {
            boolean needToReleaseRead = true;
            Stack<LockStep> steps = getPreviousSteps( lockKey );
            for ( LockStep lockStep : steps )
            {
                if ( !lockStep.isReadLockLastLocked() )
                {
                    needToReleaseRead = false;
                    break;
                }
            }

            if ( needToReleaseRead )
            {
                for ( LockStep lockStep : steps )
                {
                    getActionLock( rwLock, true ).lock();
                }
            }
        }
    }

    private Lock getActionLock( ReadWriteLock rwLock, boolean isReadAction )
    {
        if ( isReadAction )
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

    public boolean isHidden()
    {
        // paths that start with a . in any directory (or filename)
        // are considered hidden.
        // This check will catch (for example):
        // .metadata
        // /.meta/something.jar
        // /something/else/.hidden/something.jar
        if ( getPath() != null && ( getPath().indexOf( "/." ) > -1 || getPath().startsWith( "." ) ) )
        {
            return true;
        }

        return false;
    }
}
