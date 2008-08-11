package org.sonatype.nexus.proxy.item;

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

        // get the lock
        ReentrantLock lock = null;

        return new DefaultRepositoryItemUid( lock, repository, path );
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

    public void release( RepositoryItemUid uid )
    {
        if ( uid instanceof DefaultRepositoryItemUid )
        {
            ReentrantLock lock = ( (DefaultRepositoryItemUid) uid ).getLock();
        }
    }

    public int getLockCount()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
