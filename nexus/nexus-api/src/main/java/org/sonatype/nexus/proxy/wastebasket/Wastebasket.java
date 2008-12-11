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
package org.sonatype.nexus.proxy.wastebasket;

import java.io.IOException;

import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

public interface Wastebasket
{
    DeleteOperation getDeleteOperation();

    void setDeleteOperation( DeleteOperation deleteOperation );

    /**
     * Returns the count of items in wastebasket.
     * 
     * @return
     * @throws IOException
     */
    long getItemCount()
        throws IOException;

    /**
     * Returns the sum of sizes of items in the wastebasket.
     * 
     * @return
     * @throws IOException
     */
    long getSize()
        throws IOException;

    /**
     * Empties the wastebasket.
     * 
     * @throws IOException
     */
    void purge()
        throws IOException;

    /**
     * Performs a delete operation. It delets at once if item is file or link. If it is a collection, it will delete it
     * and all it's subitems (recursively).
     * 
     * @param path
     * @throws IOException
     */
    void delete( RepositoryItemUid uid, LocalRepositoryStorage ls )
        throws StorageException;
    
    /**
     * Perform a delete operation. Delete storage folder and indexer folder of a repository.
     * 
     * @param repository
     */
    public void deleteRepositoryFolders( Repository repository )
        throws IOException;
}
