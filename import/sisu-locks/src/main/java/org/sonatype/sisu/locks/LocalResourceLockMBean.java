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
import java.util.Arrays;
import java.util.List;

/**
 * Local semaphore-based {@link ResourceLockMBean} implementation.
 */
final class LocalResourceLockMBean
    extends AbstractResourceLockMBean
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ResourceLockFactory locks;

    // ----------------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------------

    LocalResourceLockMBean( final ResourceLockFactory locks )
    {
        this.locks = locks;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String[] listResourceNames()
    {
        return locks.getResourceNames();
    }

    public String[] findOwningThreads( final String name )
    {
        if ( !Arrays.asList( locks.getResourceNames() ).contains( name ) )
        {
            return new String[0]; // avoid creating new resource lock
        }
        final Thread[] owners = locks.getResourceLock( name ).getOwners();
        final String[] ownerTIDs = new String[owners.length];
        for ( int i = 0; i < owners.length; i++ )
        {
            ownerTIDs[i] = Long.toString( owners[i].getId() );
        }
        return ownerTIDs;
    }

    public String[] findWaitingThreads( final String name )
    {
        if ( !Arrays.asList( locks.getResourceNames() ).contains( name ) )
        {
            return new String[0]; // avoid creating new resource lock
        }
        final Thread[] waiters = locks.getResourceLock( name ).getWaiters();
        final String[] waiterTIDs = new String[waiters.length];
        for ( int i = 0; i < waiters.length; i++ )
        {
            waiterTIDs[i] = Long.toString( waiters[i].getId() );
        }
        return waiterTIDs;
    }

    public String[] findOwnedResources( final String tid )
    {
        final long ownerId = Long.decode( tid ).longValue();
        final List<String> names = new ArrayList<String>();
        for ( final String n : locks.getResourceNames() )
        {
            for ( final Thread t : locks.getResourceLock( n ).getOwners() )
            {
                if ( t.getId() == ownerId )
                {
                    names.add( n );
                }
            }
        }
        return names.toArray( new String[names.size()] );
    }

    public String[] findWaitedResources( final String tid )
    {
        final long waiterId = Long.decode( tid ).longValue();
        final List<String> names = new ArrayList<String>();
        for ( final String n : locks.getResourceNames() )
        {
            for ( final Thread t : locks.getResourceLock( n ).getWaiters() )
            {
                if ( t.getId() == waiterId )
                {
                    names.add( n );
                }
            }
        }
        return names.toArray( new String[names.size()] );
    }

    public void releaseResource( final String name )
    {
        if ( Arrays.asList( locks.getResourceNames() ).contains( name ) )
        {
            // forcibly unwind any current holds on this lock
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
