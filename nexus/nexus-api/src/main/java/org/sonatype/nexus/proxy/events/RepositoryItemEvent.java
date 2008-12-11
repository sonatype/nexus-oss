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
