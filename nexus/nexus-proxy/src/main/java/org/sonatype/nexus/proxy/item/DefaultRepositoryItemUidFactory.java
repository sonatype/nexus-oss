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

import java.util.HashMap;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A default factory for UIDs.
 * 
 * @author cstamas
 */
@Component( role = RepositoryItemUidFactory.class )
public class DefaultRepositoryItemUidFactory
    implements RepositoryItemUidFactory
{
    /**
     * The registry.
     */
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Override
    public RepositoryItemUid createUid( final Repository repository, String path )
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
    public RepositoryItemUid createUid( final String uidStr )
        throws IllegalArgumentException, NoSuchRepositoryException
    {
        if ( uidStr.indexOf( ":" ) > -1 )
        {
            String[] parts = uidStr.split( ":" );

            if ( parts.length == 2 )
            {
                Repository repository = repositoryRegistry.getRepository( parts[0] );

                return createUid( repository, parts[1] );
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

    // ==

    private HashMap<String, Holder> locks = new HashMap<String, Holder>();

    public synchronized RepositoryItemUidLock createUidLock( final RepositoryItemUid uid )
    {
        final String key = uid.getKey();

        Holder holder;

        if ( locks.containsKey( key ) )
        {
            holder = locks.get( key );
        }
        else
        {
            holder = new Holder( new SimpleLockResource() );

            locks.put( uid.getKey(), holder );
        }

        holder.incRefCount();

        return new DefaultRepositoryItemUidLock( this, uid, holder.getLockResource() );
    }

    public synchronized void releaseUidLock( final RepositoryItemUidLock uidLock )
    {
        final String key = uidLock.getRepositoryItemUid().getKey();

        Holder holder = locks.get( key );

        if ( holder.decRefCount() )
        {
            // TODO: some protection here
            locks.remove( key );
        }
    }

    // ==

    private static class Holder
    {
        private final LockResource lockResource;

        private int refCount;

        public Holder( LockResource lockResource )
        {
            this.lockResource = lockResource;

            this.refCount = 0;
        }

        public boolean decRefCount()
        {
            if ( refCount > 0 )
            {
                refCount--;
            }

            return refCount == 0;
        }

        public void incRefCount()
        {
            refCount++;
        }

        public LockResource getLockResource()
        {
            return lockResource;
        }
    }

}
