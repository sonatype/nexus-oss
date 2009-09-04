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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    private final ConcurrentHashMap<String, List<RepositoryItemUid>> itemUidMap =
        new ConcurrentHashMap<String, List<RepositoryItemUid>>();

    private final ConcurrentHashMap<String, ReadWriteLock> itemLockMap = new ConcurrentHashMap<String, ReadWriteLock>();

    private final ConcurrentHashMap<String, ReadWriteLock> attrLockMap = new ConcurrentHashMap<String, ReadWriteLock>();

    public RepositoryItemUid createUid( Repository repository, String path )
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

    public RepositoryItemUid createUid( String uidStr )
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

    public ReadWriteLock acquireLock( RepositoryItemUid uid )
    {
        return register( uid, itemUidMap, itemLockMap );
    }

    public void releaseLock( RepositoryItemUid uid )
    {
        deregister( uid, itemUidMap, itemLockMap );
    }

    public ReadWriteLock acquireAttributesLock( RepositoryItemUid uid )
    {
        return register( uid, itemUidMap, attrLockMap );
    }

    public void releaseAttributesLock( RepositoryItemUid uid )
    {
        deregister( uid, itemUidMap, attrLockMap );
    }

    public int getLockCount()
    {
        return itemLockMap.size();
    }

    public int getUidCount()
    {
        return itemUidMap.size();
    }

    // =====

    private synchronized ReadWriteLock register( RepositoryItemUid uid,
                                                 ConcurrentMap<String, List<RepositoryItemUid>> uidMap,
                                                 ConcurrentMap<String, ReadWriteLock> lockMap )
    {
        String key = uid.toString();

        // maintain locks
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        lockMap.putIfAbsent( key, lock );

        // maintain uidlists
        ArrayList<RepositoryItemUid> uidList = new ArrayList<RepositoryItemUid>();

        uidMap.putIfAbsent( key, uidList );

        uidMap.get( key ).add( uid );

        return lockMap.get( key );
    }

    private synchronized void deregister( RepositoryItemUid uid, ConcurrentMap<String, List<RepositoryItemUid>> uidMap,
                                          ConcurrentMap<String, ReadWriteLock> lockMap )
    {
        String key = uid.toString();

        // maintain uidlists
        if ( uidMap.containsKey( key ) )
        {
            if ( uidMap.get( key ).remove( uid ) )
            {
                if ( uidMap.get( key ).size() == 0 )
                {
                    uidMap.remove( key );

                    lockMap.remove( key );
                }
            }
        }
    }

}
