/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.sisu.locks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Local JDK {@link Locks}.
 */
@Named
@Singleton
public final class DefaultLocks
    extends AbstractLocks
{
    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Override
    protected ResourceLock createResourceLock( final String name )
    {
        return new ResourceLockImpl();
    }

    @Override
    protected LocksMBean createLocksMBean()
    {
        return new LocksMBeanImpl( this );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * {@link ResourceLock} implemented on top of a JDK {@link Semaphore}.
     */
    private static final class ResourceLockImpl
        extends AbstractSemaphoreResourceLock
    {
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------

        private final Semaphore sem;

        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------

        ResourceLockImpl()
        {
            sem = new Semaphore( Integer.MAX_VALUE, true );
        }

        // ----------------------------------------------------------------------
        // Semaphore methods
        // ----------------------------------------------------------------------

        @Override
        protected void acquire( final int permits )
        {
            sem.acquireUninterruptibly( permits );
        }

        @Override
        protected void release( final int permits )
        {
            sem.release( permits );
        }

        @Override
        protected int availablePermits()
        {
            return sem.availablePermits();
        }
    }

    private static final class LocksMBeanImpl
        implements LocksMBean
    {
        private final Locks locks;

        LocksMBeanImpl( final Locks locks )
        {
            this.locks = locks;
        }

        public String[] getResourceNames()
        {
            return locks.getResourceNames();
        }

        public long[] getResourceOwners( String name )
        {
            final Thread[] owners = locks.getResourceLock( name ).getOwners();
            final long[] ownerIds = new long[owners.length];
            for ( int i = 0; i < owners.length; i++ )
            {
                ownerIds[i] = owners[i].getId();
            }
            return ownerIds;
        }

        public long[] getResourceWaiters( String name )
        {
            final Thread[] waiters = locks.getResourceLock( name ).getWaiters();
            final long[] waiterIds = new long[waiters.length];
            for ( int i = 0; i < waiters.length; i++ )
            {
                waiterIds[i] = waiters[i].getId();
            }
            return waiterIds;
        }

        public String[] getOwnedResources( long tid )
        {
            final List<String> names = new ArrayList<String>();
            for ( final String n : locks.getResourceNames() )
            {
                for ( final Thread t : locks.getResourceLock( n ).getOwners() )
                {
                    if ( t.getId() == tid )
                    {
                        names.add( n );
                    }
                }
            }
            return names.toArray( new String[names.size()] );
        }

        public String[] getWaitedResources( long tid )
        {
            final List<String> names = new ArrayList<String>();
            for ( final String n : locks.getResourceNames() )
            {
                for ( final Thread t : locks.getResourceLock( n ).getWaiters() )
                {
                    if ( t.getId() == tid )
                    {
                        names.add( n );
                    }
                }
            }
            return names.toArray( new String[names.size()] );
        }

        public void releaseResource( String name )
        {
            final ResourceLock lock = locks.getResourceLock( name );
            for ( final Thread t : lock.getOwners() )
            {
                for ( int i = lock.getSharedCount( t ); i > 0; i-- )
                {
                    lock.unlockShared( t );
                }
                for ( int i = lock.getExclusiveCount( t ); i > 0; i-- )
                {
                    lock.unlockExclusive( t );
                }
            }
        }
    }
}
