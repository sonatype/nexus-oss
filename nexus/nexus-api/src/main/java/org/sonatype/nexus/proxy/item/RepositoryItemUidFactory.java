/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
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
