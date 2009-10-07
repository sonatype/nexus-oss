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
package org.sonatype.nexus.proxy.wastebasket;

import java.io.IOException;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
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
     * Purge the items older than the age
     * 
     * @param age age of the items to be deleted, in milliseconds
     * @throws IOException
     */
    void purge( long age )
        throws IOException;

    /**
     * Performs a delete operation. It deletes at once if item is file or link. If it is a collection, it will delete it
     * and all it's sub-items (recursively).
     * 
     * @param path
     * @throws IOException
     */
    void delete( LocalRepositoryStorage ls, Repository repository, ResourceStoreRequest request )
        throws StorageException;

    /**
     * Trash or 'rm -fr' the storage folder,'rm -fr' proxy attributes folder and index folder
     * 
     * @param repository
     * @param deleteForever 'rm -fr' the storage folder if it's true, else move the storage folder into trash
     * @throws IOException
     */
    public void deleteRepositoryFolders( Repository repository, boolean deleteForever )
        throws IOException;
}
