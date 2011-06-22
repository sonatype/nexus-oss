package org.sonatype.nexus.plugins.p2.repository.internal;

import java.io.File;

import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;

class NexusUtils
{

    private NexusUtils()
    {
    }

    static File retrieveFile( final Repository repository, final String path )
        throws LocalStorageException
    {
        final ResourceStoreRequest request = new ResourceStoreRequest( path );
        final File content =
            ( (DefaultFSLocalRepositoryStorage) repository.getLocalStorage() ).getFileFromBase( repository, request );
        return content;
    }

    static File safeRetrieveFile( final Repository repository, final String path )
    {
        try
        {
            return retrieveFile( repository, path );
        }
        catch ( final LocalStorageException e )
        {
            return null;
        }
    }

}