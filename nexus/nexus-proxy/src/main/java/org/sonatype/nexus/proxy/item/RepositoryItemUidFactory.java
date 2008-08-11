package org.sonatype.nexus.proxy.item;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;

public interface RepositoryItemUidFactory
{
    /**
     * Creates an UID based on a Repository reference and a path.
     * 
     * @param repository
     * @param path
     * @return
     */
    RepositoryItemUid createUid( Repository repository, String path );

    /**
     * Parses an "uid string representation" and creates an UID for it. Uid String representation is of form '<repoId> +
     * ':' + <path>'.
     * 
     * @param uidStr
     * @return
     * @throws IllegalArgumentException
     * @throws NoSuchRepositoryException
     */
    public RepositoryItemUid createUid( String uidStr )
        throws IllegalArgumentException,
            NoSuchRepositoryException;

    /**
     * Releases the UID from this factory.
     * 
     * @param uid
     */
    public void release( RepositoryItemUid uid );

    /**
     * For testing/debugging purposes.
     * 
     * @return
     */
    public int getLockCount();
}
