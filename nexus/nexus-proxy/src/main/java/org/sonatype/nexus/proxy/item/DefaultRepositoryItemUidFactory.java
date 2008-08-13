package org.sonatype.nexus.proxy.item;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A default factory for UIDs.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultRepositoryItemUidFactory
    implements RepositoryItemUidFactory
{
    /**
     * The registry.
     * 
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    private final ConcurrentHashMap<String, List<RepositoryItemUid>> uidMap = new ConcurrentHashMap<String, List<RepositoryItemUid>>();

    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<String, ReentrantLock>();

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

        RepositoryItemUid result = new DefaultRepositoryItemUid( this, repository, path );

        // get the lock
        register( result );

        return result;
    }

    public RepositoryItemUid createUid( String uidStr )
        throws IllegalArgumentException,
            NoSuchRepositoryException
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

    public void releaseUid( RepositoryItemUid uid )
    {
        deregister( uid );
    }

    public void lock( RepositoryItemUid uid )
    {
        lockMap.get( uid.toString() ).lock();
    }

    public void unlock( RepositoryItemUid uid )
    {
        lockMap.get( uid.toString() ).unlock();
    }

    public int getLockCount()
    {
        return lockMap.size();
    }

    // =====

    private synchronized void register( RepositoryItemUid uid )
    {
        String key = uid.toString();

        // maintain locks
        ReentrantLock lock = new ReentrantLock();

        lockMap.putIfAbsent( key, lock );

        // maintain uidlists
        ArrayList<RepositoryItemUid> uidList = new ArrayList<RepositoryItemUid>();

        uidMap.putIfAbsent( key, uidList );

        uidMap.get( key ).add( uid );
    }

    private synchronized void deregister( RepositoryItemUid uid )
    {
        String key = uid.toString();

        // maintain uidlists
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
