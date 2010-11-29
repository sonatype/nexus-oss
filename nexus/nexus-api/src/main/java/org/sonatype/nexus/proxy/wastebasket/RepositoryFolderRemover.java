package org.sonatype.nexus.proxy.wastebasket;

import java.io.IOException;

import org.sonatype.nexus.proxy.repository.Repository;

public interface RepositoryFolderRemover
{
    /**
     * Trash or 'rm -fr' the storage folder,'rm -fr' proxy attributes folder and index folder
     * 
     * @param repository
     * @param deleteForever 'rm -fr' the storage folder if it's true, else move the storage folder into trash
     * @throws IOException
     */
    void deleteRepositoryFolders( Repository repository, boolean deleteForever )
        throws IOException;
}
