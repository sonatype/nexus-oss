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
 * A simple filter a la FileFilter, for filtering which items should be processed. Note: the StoreWalker does RECURSIVE
 * processing, hence unlike FileFilter, if the current collection is filtered out, but the filter says diveIn, it will
 * dive in deeper.
 * 
 * @author cstamas
 */
public interface WalkerFilter
{
    /**
     * This method is called for every item implementation. If returns fals, it will not initate any call against to for
     * processing (ie. collEnter, processItem, callExit). But the recursive processing of this item (in case it is
     * Collection) is not affected by this method!
     * 
     * @param context
     * @param item
     * @return
     */
    boolean shouldProcess( WalkerContext context, StorageItem item );

    /**
     * In case of Collections, StoreWalker will ask should it process those recursively. This is a place to "cut" the
     * tree walking if needed.
     * 
     * @paramt context
     * @param coll
     * @return
     */
    boolean shouldProcessRecursively( WalkerContext context, StorageCollectionItem coll );
}
