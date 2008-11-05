/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.attributes;

import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

/**
 * The Interface AttributeStorage, used by LocalStorages.
 * 
 * @see LocalRepositoryStorage
 * @author cstamas
 */
public interface AttributeStorage
    extends EventListener
{
    /**
     * Gets the attributes.
     * 
     * @param uid the uid
     * @return the attributes
     */
    AbstractStorageItem getAttributes( RepositoryItemUid uid );

    /**
     * Put attribute.
     * 
     * @param item the item
     */
    void putAttribute( StorageItem item );

    /**
     * Delete attributes.
     * 
     * @param uid the uid
     * @return true, if successful
     */
    boolean deleteAttributes( RepositoryItemUid uid );

}
