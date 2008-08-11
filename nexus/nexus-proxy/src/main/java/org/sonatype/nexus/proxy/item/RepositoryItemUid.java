package org.sonatype.nexus.proxy.item;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Repository item UID represents a key that uniquely identifies a resource in a repository. Every Item originating from
 * Nexus, that is not "virtual" is backed by UID with reference to it's originating Repository and path within that
 * repository. UIDs are immutable.
 * 
 * @author cstamas
 */
public interface RepositoryItemUid
{
    /** Constant to denote a separator in Proximity paths. */
    String PATH_SEPARATOR = "/";

    /** Constant to represent a root of the path. */
    String PATH_ROOT = PATH_SEPARATOR;

    /**
     * Gets the repository that is the origin of the item identified by this UID.
     * 
     * @return
     */
    Repository getRepository();

    /**
     * Gets the path that is the original path in the origin repository for resource with this UID.
     * 
     * @return
     */
    String getPath();

    /**
     * Performs a lock on this UID, and by that, potentionally locks all other threads if needed, that are working on
     * the same UID as this one.
     */
    void lock();

    /**
     * Unlocks this UID.
     */
    void unlock();
}
