/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.plexus.plugin.ExtensionPoint;

/**
 * A Shadow Repository is a special repository type that usually points to a master repository and transforms it in some
 * way (look at Maven1 to Maven2 layout changing repo).
 * 
 * @author cstamas
 */
@ExtensionPoint
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
    String getMasterRepositoryId();

    /**
     * Sets the master repository of this ShadowRepository.
     * 
     * @param masterRepository
     * @throws IncompatibleMasterRepositoryException
     */
    void setMasterRepositoryId( String masterRepositoryId )
        throws NoSuchRepositoryException,
            IncompatibleMasterRepositoryException;

    /**
     * Gets sync at startup.
     * 
     * @return
     */
    boolean isSynchronizeAtStartup();

    /**
     * Sets sync at start.
     * 
     * @param value
     */
    void setSynchronizeAtStartup( boolean value );

    /**
     * Returns the master.
     * 
     * @return
     */
    Repository getMasterRepository();

    /**
     * Sets the master.
     * 
     * @return
     */
    void setMasterRepository( Repository repository )
        throws IncompatibleMasterRepositoryException;

    /**
     * Triggers syncing with master repository.
     */
    void synchronizeWithMaster();
}
