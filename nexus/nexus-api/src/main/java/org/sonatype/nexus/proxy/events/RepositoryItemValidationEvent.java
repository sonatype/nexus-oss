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
package org.sonatype.nexus.proxy.events;

import java.util.Map;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Abstract super class for validation related events.
 * 
 * @author cstamas
 * @since 1.10.0
 */
public abstract class RepositoryItemValidationEvent
    extends RepositoryEvent
{
    /** The item in question */
    private final StorageItem item;

    private final Map<String, Object> itemContext;

    public RepositoryItemValidationEvent( final Repository repository, final StorageItem item )
    {
        super( repository );
        this.item = item;
        this.itemContext = item.getItemContext().flatten();
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
     * Gets the item context. A snapshot of item.getItemContext() in creation moment of this event, since
     * item.getItemContenxt() is mutable is is probably changed when some async processor will process this event!
     * 
     * @return the item context
     */
    public Map<String, Object> getItemContext()
    {
        return itemContext;
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
