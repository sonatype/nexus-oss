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
package org.sonatype.nexus.proxy.item;

import org.sonatype.nexus.proxy.access.Action;
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
     * Locks this UID for action. Will perform lock upgrade is needed (read -> write).
     * 
     * @param action
     */
    void lock( Action action );

    /**
     * Unlocks UID.
     */
    void unlock();

    /**
     * Locks attributes of item belonging to this UID for action. Will perform lock upgrade is needed (read -> write).
     * 
     * @param action
     */
    void lockAttributes( Action action );

    /**
     * Unlocks attributes of item belonging to this UID.
     */
    void unlockAttributes();
}
