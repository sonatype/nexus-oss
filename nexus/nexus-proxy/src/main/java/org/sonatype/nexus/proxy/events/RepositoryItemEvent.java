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
package org.sonatype.nexus.proxy.events;

import java.util.Map;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * The event fired in case of some content changes in Nexus related to an item/file.
 * 
 * @author cstamas
 */
public abstract class RepositoryItemEvent
    extends RepositoryEvent
{
    /** The item in question */
    private final StorageItem item;

    public RepositoryItemEvent( final StorageItem item )
    {
        super( item.getRepositoryItemUid().getRepository() );

        this.item = item;
    }

    /**
     * Gets the item uid. Shortcut for item.getRepositoryItemUid().
     * 
     * @return the item uid
     */
    public RepositoryItemUid getItemUid()
    {
        return item.getRepositoryItemUid();
    }

    /**
     * Gets the item context. Shortcut for item.getItemContext().
     * 
     * @return the item context
     */
    public Map<String, Object> getContext()
    {
        return item.getItemContext();
    }

    /**
     * Gets the involved item.
     * 
     * @return
     */
    public StorageItem getItem()
    {
        return item;
    }

}
