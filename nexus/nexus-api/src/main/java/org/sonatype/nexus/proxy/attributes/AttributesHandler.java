/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.attributes;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * AttributesHandler manages the storage of item attributes, does their recalculation if needed (how it's done is left
 * to implementation) and offers some "shortcut" methods to perform updates on some (core used) attributes like
 * "lastRequested" and other. All {@link org.sonatype.nexus.proxy.item.StorageCollectionItem} types does not have
 * persisted attributes, and handler will simply not handle them.
 *
 * @author cstamas
 */
public interface AttributesHandler
{

    /**
     * Returns the AttributeStorage used by this AttributesHandler instance.
     *
     * @return AttributeStorage used by this instance.
     */
    AttributeStorage getAttributeStorage();

    /**
     * Sets the attribute storage used by this instance.
     *
     * @param attributeStorage
     * @deprecated For UT uses only!
     */
    @Deprecated
    void setAttributeStorage( AttributeStorage attributeStorage );

    /**
     * Fetches the item attributes and decorates the supplied item instance with it.
     *
     * @param item the item to have attributes loaded up and decorated with.
     */
    void fetchAttributes( StorageItem item );

    /**
     * Stores the item attributes. If non-null content locator is supplied, attributes will be recalculated before saving.
     *
     * @param item    the item
     * @param content the content of the item if we deal with StorageFileItem
     */
    void storeAttributes( StorageItem item, ContentLocator content );

    /**
     * Stores item attributes, as-is, will not try attribute expansion either.
     *
     * @param item the item
     */
    void storeAttributes( StorageItem item );

    /**
     * Removes the item attributes from attribute storage.
     *
     * @param uid the uid
     * @return true if attributes are found and deleted, false otherwise.
     */
    boolean deleteAttributes( RepositoryItemUid uid );

    /**
     * Sets the storageItem's "checkedRemotely" attribute (a timestamp in millis) to the passed in one and persists
     * the modified attributes.
     *
     * @param timestamp
     * @param storageItem
     */
    void touchItemCheckedRemotely( long timestamp, StorageItem storageItem );

    /**
     * Sets the storageItem's "lastRequested" attribute (a timestamp in millis) to the passed in one and persists
     * the modified attributes.
     *
     * @param timestamp
     * @param storageItem
     */
    void touchItemLastRequested( long timestamp, StorageItem storageItem );

    // ==

    /**
     * Updates the "checkedRemotely" attribute of item in given repository Touch item and sets on it the current time.
     *
     * @deprecated Use {@link #touchItemCheckedRemotely(long, org.sonatype.nexus.proxy.item.StorageItem)} instead.
     */
    @Deprecated
    void touchItemRemoteChecked( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, LocalStorageException;

    /**
     * Touch item and sets on it the given timestamp.
     *
     * @deprecated Use {@link #touchItemCheckedRemotely(long, org.sonatype.nexus.proxy.item.StorageItem)} instead.
     */
    @Deprecated
    void touchItemRemoteChecked( long timestamp, Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, LocalStorageException;

    /**
     * Touch item last requested and sets on it the current time.
     *
     * @deprecated Use {@link #touchItemLastRequested(long, org.sonatype.nexus.proxy.item.StorageItem)} instead.
     */
    @Deprecated
    void touchItemLastRequested( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, LocalStorageException;

    /**
     * Touch item last requested and sets on it the given timestamp.
     *
     * @deprecated Use {@link #touchItemLastRequested(long, org.sonatype.nexus.proxy.item.StorageItem)} instead.
     */
    void touchItemLastRequested( long timestamp, Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, LocalStorageException;

    /**
     * Touch only if request is user-request (coming from outside of nexus).
     *
     * @deprecated Use {@link #touchItemLastRequested(long, org.sonatype.nexus.proxy.item.StorageItem)} instead.
     */
    void touchItemLastRequested( long timestamp, Repository repository, ResourceStoreRequest request,
                                 StorageItem storageItem )
        throws ItemNotFoundException, LocalStorageException;

    /**
     * Update item attributes, does not modify the content of it.
     *
     * @deprecated Use {@link #storeAttributes(org.sonatype.nexus.proxy.item.StorageItem)} instead!
     */
    @Deprecated
    void updateItemAttributes( Repository repository, ResourceStoreRequest request, StorageItem item )
        throws ItemNotFoundException, LocalStorageException;
}
