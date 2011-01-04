/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.wastebasket;

import java.io.IOException;

import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.statistics.DeferredLong;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

public interface Wastebasket
{
    /**
     * Returns the delete operation that this Wastebasket operates upon.
     * 
     * @return
     */
    DeleteOperation getDeleteOperation();

    /**
     * Sets the delete operation to have this Wastebasket operate.
     * 
     * @param deleteOperation
     */
    void setDeleteOperation( DeleteOperation deleteOperation );

    /**
     * Returns the sum of sizes of items in the wastebasket.
     * 
     * @return
     * @throws IOException
     */
    DeferredLong getTotalSize();

    /**
     * Empties the wastebasket.
     * 
     * @throws IOException
     */
    void purgeAll()
        throws IOException;

    /**
     * Purge the items older than the age
     * 
     * @param age age of the items to be deleted, in milliseconds
     * @throws IOException
     */
    void purgeAll( long age )
        throws IOException;

    /**
     * Returns the sum of sizes of items in the wastebasket.
     * 
     * @return
     * @throws IOException
     */
    DeferredLong getSize( Repository repository );

    /**
     * Empties the wastebasket.
     * 
     * @throws IOException
     */
    void purge( Repository repository )
        throws IOException;

    /**
     * Purge the items older than the age
     * 
     * @param age age of the items to be deleted, in milliseconds
     * @throws IOException
     */
    void purge( Repository repository, long age )
        throws IOException;

    /**
     * Performs a delete operation. It deletes at once if item is file or link. If it is a collection, it will delete it
     * and all it's sub-items (recursively).
     * 
     * @param path
     * @throws IOException
     */
    void delete( LocalRepositoryStorage ls, Repository repository, ResourceStoreRequest request )
        throws LocalStorageException;

    /**
     * Performs an un-delete operation. If target (where undeleted item should be returned) exists, false is returned,
     * true otherwise. It undeletes at once if item is file or link. If it is a collection, it will undelete it and all
     * it's sub-items (recursively).
     * 
     * @param path
     * @throws IOException
     */
    boolean undelete( LocalRepositoryStorage ls, Repository repository, ResourceStoreRequest request )
        throws LocalStorageException;
}
