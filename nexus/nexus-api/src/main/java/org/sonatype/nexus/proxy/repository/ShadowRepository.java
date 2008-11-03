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
    ContentClass getMasterRepositoryContentClass();

    Repository getMasterRepository();

    public void setMasterRepository( Repository masterRepository )
        throws IncompatibleMasterRepositoryException;

    public void synchronizeWithMaster();
}
