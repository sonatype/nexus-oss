package org.sonatype.nexus.proxy.wastebasket;

import java.io.IOException;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A component doing "cleanup" (after a repository removal) of anything needed, for example removing directories or so.
 * 
 * @author cstamas
 */
public interface RepositoryFolderCleaner
{
    /**
     * Performs the needed cleanup after repository already removed from system, that had ID as passed in.
     * 
     * @param repository the repository removed (WARNING: RepositoryRegistry does not contains it anymore!).
     * @param deleteForever true if removal wanted, false if just "move to trash".
     * @throws IOException
     */
    void cleanRepositoryFolders( Repository repository, boolean deleteForever )
        throws IOException;
}
