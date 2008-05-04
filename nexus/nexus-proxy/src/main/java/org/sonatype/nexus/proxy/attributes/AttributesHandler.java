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

import java.io.InputStream;
import java.util.Map;

import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Interface AttributesHandler. Used by LocalStorage to decorate the items.
 */
public interface AttributesHandler
{
    
    String ROLE = AttributesHandler.class.getName();

    AttributeStorage getAttributeStorage();

    void setAttributeStorage( AttributeStorage attributeStorage );

    /**
     * Fetches the item attributes and decorates the supplied item.
     * 
     * @param item the item
     * @return Map of attributes or empty map if none found.
     */
    void fetchAttributes( AbstractStorageItem item );

    /**
     * Creates the item attributes and stores them.
     * 
     * @param item the item
     * @param inputStream the input stream
     */
    void storeAttributes( AbstractStorageItem item, InputStream inputStream );

    /**
     * Removes the item attributes.
     * 
     * @param uid the uid
     * @return true if attributes are found and deleted, false otherwise.
     */
    boolean deleteAttributes( RepositoryItemUid uid );

    /**
     * Recreates the attributes for all items in the given storage.
     * 
     * @param initialData the initial data
     * @param repository the repository
     */
    void recreateAttributes( Repository repository, Map<String, String> initialData );

}
