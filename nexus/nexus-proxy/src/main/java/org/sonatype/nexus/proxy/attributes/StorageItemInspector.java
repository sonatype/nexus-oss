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

import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * The Interface StorageItemInspector.
 */
public interface StorageItemInspector
{

    String ROLE = StorageItemInspector.class.getName();

    /**
     * Checks if item is handled.
     * 
     * @param item the item
     * 
     * @return true, if is handled
     */
    boolean isHandled( StorageItem item );

    /**
     * Process storage item.
     * 
     * @param item the item
     * 
     * @throws Exception the exception
     */
    void processStorageItem( StorageItem item )
        throws Exception;

}
