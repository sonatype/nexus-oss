/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.proxy.router;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Repository Router interface. This router offers a simple API to request items from Proximity. It calculates and
 * knows which repositories are registered within Proximity.
 * 
 * @see Repository
 * @see RepositoryRegistry
 * @author cstamas
 */
public interface RepositoryRouter
    extends ResourceStore, EventListener
{
    /**
     * The content class that is handled by this router.
     * 
     * @return
     */
    ContentClass getHandledContentClass();

    /**
     * Dereferences the link.
     * 
     * @param item
     * @return
     * @throws AccessDeniedException
     * @throws ItemNotFoundException
     * @throws RepositoryNotAvailableException
     * @throws StorageException
     */
    StorageItem dereferenceLink( StorageLinkItem item )
        throws NoSuchResourceStoreException,
            AccessDeniedException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException;

    /**
     * Storing an item in router is simply "spoofing" all repo items beneath that path.
     * 
     * @param path
     * @param is
     */
    void storeItem( String path, InputStream is )
        throws IOException;

    /**
     * Deletes an item from the router. See storeItem(path, is)
     * 
     * @param path
     * @throws IOException
     */
    void deleteItem( String path )
        throws IOException;
}
