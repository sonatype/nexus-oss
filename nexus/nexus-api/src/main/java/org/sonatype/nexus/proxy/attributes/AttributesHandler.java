/**
 * Sonatype NexusTM [Open Source Version].
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
package org.sonatype.nexus.proxy.attributes;

import java.io.InputStream;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * The Interface AttributesHandler. Used by LocalStorage to decorate the items.
 */
public interface AttributesHandler
{
    AttributeStorage getAttributeStorage();

    void setAttributeStorage( AttributeStorage attributeStorage );

    /**
     * Fetches the item attributes and decorates the supplied item.
     * 
     * @param item the item
     * @return Map of attributes or empty map if none found.
     */
    void fetchAttributes( StorageItem item );

    /**
     * Creates the item attributes and stores them.
     * 
     * @param item the item
     * @param inputStream the input stream
     */
    void storeAttributes( StorageItem item, InputStream inputStream );

    /**
     * Removes the item attributes.
     * 
     * @param uid the uid
     * @return true if attributes are found and deleted, false otherwise.
     */
    boolean deleteAttributes( RepositoryItemUid uid );
}
