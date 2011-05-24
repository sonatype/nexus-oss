/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.item;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.access.Action;

public class DefaultRepositoryItemUidLock
    implements RepositoryItemUidLock
{
    private final RepositoryItemUidFactory factory;

    private final RepositoryItemUid uid;

    private final LockResource contentLock;

    private final AtomicInteger released;

    protected DefaultRepositoryItemUidLock( final RepositoryItemUidFactory factory, final RepositoryItemUid uid,
                                            final LockResource contentLock )
    {
        super();

        this.factory = factory;

        this.uid = uid;

        this.contentLock = contentLock;

        this.released = new AtomicInteger( 0 );
    }

    @Override
    public RepositoryItemUid getRepositoryItemUid()
    {
        return uid;
    }

    @Override
    public void lock( final Action action )
        throws IllegalStateException
    {
        if ( released.get() != 0 )
        {
            throw new IllegalStateException(
                "This instance of DefaultRepositoryItemUidLock has been released, it is not usable for locking anymore!" );
        }

        if ( action.isReadAction() )
        {
            contentLock.lockShared();
        }
        else
        {
            contentLock.lockExclusively();
        }
    }

    public void unlock()
        throws IllegalStateException
    {
        if ( released.get() != 0 )
        {
            throw new IllegalStateException(
                "This instance of DefaultRepositoryItemUidLock has been released, it is not usable for locking anymore!" );
        }

        contentLock.unlock();
    }

    @Override
    public void release()
    {
        if ( released.compareAndSet( 0, 1 ) )
        {
            factory.releaseUidLock( this );
        }
    }

    // ==

    // this below is mere for "transition period" to ease debugging or leak detection

    private static Logger logger = LoggerFactory.getLogger( DefaultRepositoryItemUidLock.class );

    public void finalize()
        throws Throwable
    {
        try
        {
            if ( released.compareAndSet( 0, 1 ) )
            {
                factory.releaseUidLock( this );

                logger.error( "Memory leak: UIDLock for UID {} not released properly, lock status is {}!",
                    getRepositoryItemUid(), contentLock.toString() );
            }
        }
        finally
        {
            super.finalize();
        }
    }

    // for Debug/tests vvv

    protected LockResource getContentLock()
    {
        return contentLock;
    }

    // for Debug/tests ^^^

}
