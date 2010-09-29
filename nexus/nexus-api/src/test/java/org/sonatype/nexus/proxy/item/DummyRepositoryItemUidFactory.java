package org.sonatype.nexus.proxy.item;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;

public class DummyRepositoryItemUidFactory
    implements RepositoryItemUidFactory
{
    private Map<String, DefaultRepositoryItemUid> uids = new HashMap<String, DefaultRepositoryItemUid>();

    public synchronized DefaultRepositoryItemUid createUid( Repository repository, String path )
    {
        String key = repository.getId() + ":" + path;

        if ( !uids.containsKey( key ) )
        {
            uids.put( key, new DefaultRepositoryItemUid( this, repository, path ) );
        }

        return uids.get( key );
    }

    public RepositoryItemUid createUid( String uidStr )
        throws IllegalArgumentException, NoSuchRepositoryException
    {
        throw new UnsupportedOperationException(
            "This dummy factory does not supports this method (it needs repo registry et al)" );
    }

    public Map<String, RepositoryItemUid> getActiveUidMapSnapshot()
    {
        return new HashMap<String, RepositoryItemUid>( uids );
    }
}
