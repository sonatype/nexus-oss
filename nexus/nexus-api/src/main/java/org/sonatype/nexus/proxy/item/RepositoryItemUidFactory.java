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
     * Performs a lock on this UID, and by that, potentionally locks all other threads if needed, that are working on
     * the same UID as this one.
     */
    void lock( RepositoryItemUid uid );

    /**
     * Unlocks this UID.
     */
    void unlock( RepositoryItemUid uid );
    
    /**
     * For testing/debugging purposes.
     * 
     * @return
     */
    public int getLockCount();
    
    /**
     * For testing/debugging purposes.
     * 
     * @return
     */
    public int getUidCount();
}
