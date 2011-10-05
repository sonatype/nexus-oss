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

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.StandardMBean;

import org.sonatype.sisu.locks.Locks.ResourceLock;

/**
 * Local {@link LocksMBean} implementation.
 */
final class LocalLocksMBeanImpl
    extends StandardMBean
    implements LocksMBean
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Locks locks;

    // ----------------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------------

    LocalLocksMBeanImpl( final Locks locks )
    {
        super( LocksMBean.class, false );
        this.locks = locks;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String[] getResourceNames()
    {
        return locks.getResourceNames();
    }

    public String[] getOwningThreads( final String name )
    {
        final Thread[] owners = locks.getResourceLock( name ).getOwners();
        final String[] ownerTIDs = new String[owners.length];
        for ( int i = 0; i < owners.length; i++ )
        {
            ownerTIDs[i] = Long.toString( owners[i].getId() );
        }
        return ownerTIDs;
    }

    public String[] getWaitingThreads( final String name )
    {
        final Thread[] waiters = locks.getResourceLock( name ).getWaiters();
        final String[] waiterTIDs = new String[waiters.length];
        for ( int i = 0; i < waiters.length; i++ )
        {
            waiterTIDs[i] = Long.toString( waiters[i].getId() );
        }
        return waiterTIDs;
    }

    public String[] getOwnedResources( final String tid )
    {
        final long ownerId = Integer.parseInt( tid );
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

    public String[] getWaitedResources( final String tid )
    {
        final long waiterId = Integer.parseInt( tid );
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

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    @Override
    protected String getParameterName( final MBeanOperationInfo op, final MBeanParameterInfo param, final int seq )
    {
        return op.getName().endsWith( "Resources" ) ? "thread id #" : "resource name";
    }
}
