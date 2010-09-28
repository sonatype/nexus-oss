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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

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

    private final ConcurrentHashMap<String, WeakReference<RepositoryItemUid>> itemUidMap =
        new ConcurrentHashMap<String, WeakReference<RepositoryItemUid>>();

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

        String key = repository.getId() + ":" + path;

        RepositoryItemUid newGuy = new DefaultRepositoryItemUid( this, repository, path );

        itemUidMap.putIfAbsent( key, new WeakReference<RepositoryItemUid>( newGuy ) );

        WeakReference<RepositoryItemUid> ref = itemUidMap.get( key );

        RepositoryItemUid toBeReturned = null;

        if ( ref != null )
        {
            toBeReturned = ref.get();
        }

        if ( toBeReturned != null )
        {
            // we have an UID instance found "alive" in the map
            cleanUpItemUidMap( false );

            return toBeReturned;
        }
        else
        {
            synchronized ( itemUidMap )
            {
                // we have not found an UID instance in the map, we have to make one and stick in into map

                // try it again, since we were maybe sitting there waiting for someone who already did the job
                itemUidMap.putIfAbsent( key, new WeakReference<RepositoryItemUid>( newGuy ) );

                toBeReturned = itemUidMap.get( key ).get();

                if ( toBeReturned != null )
                {
                    return toBeReturned;
                }

                // still no luck, do it
                itemUidMap.put( key, new WeakReference<RepositoryItemUid>( newGuy ) );

                toBeReturned = newGuy;

                // do cleansing of the map if needed, this call might do nothing or clean up the itemUidMap for gc'ed
                // UIDs
                cleanUpItemUidMap( false );

                return toBeReturned;
            }
        }
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

    public Map<String, RepositoryItemUid> getActiveUidMapSnapshot()
    {
        return Collections.emptyMap();
    }

    /**
     * Used in UTs only, NOT public method!
     * 
     * @return
     */
    public int getUidCount( boolean forceClean )
    {
        if ( forceClean )
        {
            cleanUpItemUidMap( true );
        }

        return itemUidMap.size();
    }

    // ==

    // =v=v=v=v=v=v= This part here probably needs polishing: the retention should depend on load too =v=v=v=v=v=v=

    private static final long ITEM_UID_MAP_RETENTION_TIME = 5000;

    private volatile long lastClearedItemUidMap;

    private final ReentrantLock cleanupLock = new ReentrantLock();

    private void cleanUpItemUidMap( boolean force )
    {
        // just try to lock it. If we cannot lock it, leave it, since some other thread is
        // already doing cleanup. But even if we do succeed in locking the cleanupLock, we have the
        // time barrier, that have to be true, to do actual cleanup.
        if ( cleanupLock.tryLock() )
        {
            // we are in, that means no one else is doing cleanup, and also that
            // we acquired the lock in the same time.
            // still, it is undecided yet, will we actually perform the cleanup, since
            // we have a time barrier to pass also (unless force=true).
            try
            {
                long now = System.currentTimeMillis();

                if ( force || ( now - lastClearedItemUidMap > ITEM_UID_MAP_RETENTION_TIME ) )
                {
                    lastClearedItemUidMap = now;

                    for ( Iterator<ConcurrentMap.Entry<String, WeakReference<RepositoryItemUid>>> i =
                        itemUidMap.entrySet().iterator(); i.hasNext(); )
                    {
                        ConcurrentMap.Entry<String, WeakReference<RepositoryItemUid>> entry = i.next();

                        if ( entry.getValue().get() == null )
                        {
                            i.remove();
                        }
                    }
                }
            }
            finally
            {
                cleanupLock.unlock();
            }
        }
    }

    // =^=^=^=^=^=^= This part here probably needs polishing: the retention should depend on load too =^=^=^=^=^=^=

}
