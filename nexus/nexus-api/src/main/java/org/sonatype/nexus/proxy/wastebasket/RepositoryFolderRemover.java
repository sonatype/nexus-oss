package org.sonatype.nexus.proxy.wastebasket;

import java.io.IOException;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A "hub" for invoking all registered RepositoryFolderCleaners.
 * 
 * @author cstamas
 */
public interface RepositoryFolderRemover
{
    void deleteRepositoryFolders( Repository repository, boolean deleteForever )
        throws IOException;
}
