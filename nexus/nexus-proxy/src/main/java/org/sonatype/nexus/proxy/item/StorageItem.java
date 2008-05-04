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
package org.sonatype.nexus.proxy.item;

import java.util.Map;

/**
 * The Interface StorageItem, a top of the item abstraction.
 */
public interface StorageItem
{

    /**
     * Gets the repository item uid from where originates item.
     * 
     * @return the repository item uid
     */
    RepositoryItemUid getRepositoryItemUid();

    /**
     * Gets the repository id from where originates item.
     * 
     * @return the repository id
     */
    String getRepositoryId();

    /**
     * Gets the creation time.
     * 
     * @return the created
     */
    long getCreated();

    /**
     * Gets the modification time.
     * 
     * @return the modified
     */
    long getModified();

    /**
     * Gets the stored locally time.
     * 
     * @return the stored locally
     */
    long getStoredLocally();

    /**
     * Gets the remote checked.
     * 
     * @return the remote checked
     */
    long getLastTouched();

    /**
     * Checks if is virtual.
     * 
     * @return true, if is virtual
     */
    boolean isVirtual();

    /**
     * Checks if is readable.
     * 
     * @return true, if is readable
     */
    boolean isReadable();

    /**
     * Checks if is writable.
     * 
     * @return true, if is writable
     */
    boolean isWritable();

    /**
     * Returns true if the item is expired.
     */
    boolean isExpired();

    /**
     * Gets the path.
     * 
     * @return the path
     */
    String getPath();

    /**
     * Gets the name.
     * 
     * @return the name
     */
    String getName();

    /**
     * Gets the parent path.
     * 
     * @return the parent path
     */
    String getParentPath();

    /**
     * Gets the remote url.
     * 
     * @return the remote url
     */
    String getRemoteUrl();

    /**
     * Gets the user attributes. These are saved and persisted.
     * 
     * @return the attributes
     */
    Map<String, String> getAttributes();

    /**
     * Gets the item context. It is living only during item processing, it is not stored.
     * 
     * @return the attributes
     */
    Map<String, Object> getItemContext();

    /**
     * Overlay.
     * 
     * @param item the item
     */
    void overlay( StorageItem item );

}
