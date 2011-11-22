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

import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

/**
 * The Interface AttributeStorage, used by LocalStorages.
 * 
 * @see LocalRepositoryStorage
 * @author cstamas
 */
public interface AttributeStorage
{
    /**
     * Gets the attributes for given key.
     * 
     * @param uid the key for which attributes needs to be fetched
     * @return the attributes
     */
    Attributes getAttributes( RepositoryItemUid uid );

    /**
     * Put attributes for given key.
     * 
     * @param uid the key
     * @param attributes the attributes to store
     */
    void putAttributes( RepositoryItemUid uid, Attributes attributes );

    /**
     * Delete attributes associated with given key.
     * 
     * @param uid the uid
     * @return true, if delete actually happened
     */
    boolean deleteAttributes( RepositoryItemUid uid );
}
