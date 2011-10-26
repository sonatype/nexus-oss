/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.locks.ResourceLockFactory;

/**
 * Abstract factory for UIDs.
 * 
 * @author cstamas
 */
public abstract class AbstractRepositoryItemUidFactory
    implements RepositoryItemUidFactory, Disposable
{
    @Override
    public DefaultRepositoryItemUid createUid( final Repository repository, String path )
    {
        // path corrections
        if ( !StringUtils.isEmpty( path ) )
        {
            if ( !path.startsWith( RepositoryItemUid.PATH_ROOT ) )
            {
                path = RepositoryItemUid.PATH_ROOT + path;
            }
        }
        else
        {
            path = RepositoryItemUid.PATH_ROOT;
        }

        return new DefaultRepositoryItemUid( this, repository, path );
    }

    @Override
    public abstract DefaultRepositoryItemUid createUid( final String uidStr )
        throws IllegalArgumentException, NoSuchRepositoryException;

    // ==

    @Inject
    @Nullable
    @Named( "${sisu-resource-locks:-disabled}" )
    private ResourceLockFactory sisuLockFactory;

    private WeakHashMap<DefaultRepositoryItemUidLock, WeakReference<DefaultRepositoryItemUidLock>> locks =
        new WeakHashMap<DefaultRepositoryItemUidLock, WeakReference<DefaultRepositoryItemUidLock>>();

    @Override
    public DefaultRepositoryItemUidLock createUidLock( final RepositoryItemUid uid )
    {
        final String key = new String( uid.getKey() );

        return doCreateUidLockForKey( key );
    }

    @Override
    public DefaultRepositoryItemUidLock createUidAttributeLock( final RepositoryItemUid uid )
    {
        final String key = new String( "attribute:" + uid.getKey() );

        return doCreateUidLockForKey( key );
    }

    // ==

    protected synchronized DefaultRepositoryItemUidLock doCreateUidLockForKey( final String key )
    {
        final LockResource lockResource;
        if ( sisuLockFactory != null )
        {
            lockResource = new SisuLockResource( sisuLockFactory.getResourceLock( key ) );
        }
        else
        {
            lockResource = new SimpleLockResource();
        }

        final DefaultRepositoryItemUidLock newLock = new DefaultRepositoryItemUidLock( key, lockResource );

        final WeakReference<DefaultRepositoryItemUidLock> oldLockRef = locks.get( newLock );

        if ( oldLockRef != null )
        {
            final RepositoryItemUidLock oldLock = oldLockRef.get();

            if ( oldLock != null )
            {
                return oldLockRef.get();
            }
        }

        locks.put( newLock, new WeakReference<DefaultRepositoryItemUidLock>( newLock ) );

        return newLock;
    }

    /**
     * For UTs, not to be used in production code!
     * 
     * @return
     */
    protected int locksInMap()
    {
        return locks.size();
    }

    @Override
    public void dispose()
    {
        if ( sisuLockFactory != null )
        {
            sisuLockFactory.shutdown();
        }
    }
}
