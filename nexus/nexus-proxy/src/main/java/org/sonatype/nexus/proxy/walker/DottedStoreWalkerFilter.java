/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A simple walkerFilter that filters out all items (directories and files) that starts with dot ('.'), aka "hidden"
 * files.
 *
 * @author cstamas
 * @author Alin Dreghiciu
 */
public class DottedStoreWalkerFilter
    implements WalkerFilter
{

    public boolean shouldProcess( WalkerContext ctx, StorageItem item )
    {
        return shouldProcessItem( item );
    }

    public boolean shouldProcessRecursively( WalkerContext ctx, StorageCollectionItem coll )
    {
        return shouldProcessItem( coll );
    }

    protected boolean shouldProcessItem( StorageItem item )
    {
        return !item.getName().startsWith( "." );
    }

    /**
     * Builder method.
     *
     * @return new DottedStoreWalkerFilter
     */
    public static DottedStoreWalkerFilter excludeItemsStartingWithDot()
    {
        return new DottedStoreWalkerFilter();
    }

}
