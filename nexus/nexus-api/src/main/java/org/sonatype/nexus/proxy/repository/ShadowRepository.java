/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
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
