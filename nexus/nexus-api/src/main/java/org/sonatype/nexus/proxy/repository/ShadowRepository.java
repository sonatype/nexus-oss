package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * A Shadow Repository is a special repository type that usually points to a master repository and transforms it in some
 * way (look at Maven1 to Maven2 layout changing repo).
 * 
 * @author cstamas
 */
public interface ShadowRepository
    extends Repository
{
    /**
     * The content class that is expected to have the repository set as master for this ShadowRepository.
     * 
     * @return
     */
    ContentClass getMasterRepositoryContentClass();

    /**
     * Returns the master repository of this ShadowRepository.
     * 
     * @return
     */
    Repository getMasterRepository();

    /**
     * Sets the master repository of this ShadowRepository.
     * 
     * @param masterRepository
     * @throws IncompatibleMasterRepositoryException
     */
    public void setMasterRepository( Repository masterRepository )
        throws IncompatibleMasterRepositoryException;

    /**
     * Triggers syncing with master repository.
     */
    public void synchronizeWithMaster();
}
