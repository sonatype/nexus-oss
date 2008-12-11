/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.utils;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A simple filter a la FileFilter, for filtering which items should be processed. Note: the StoreWalker does RECURSIVE
 * processing, hence unlike FileFilter, if the current collection is filtered out, but the filter says diveIn, it will
 * dive in deeper.
 * 
 * @author cstamas
 * @deprecated use Walker service in org.sonatype.nexus.proxy.walker package
 */
public interface StoreWalkerFilter
{
    /**
     * This method is called for every item implementation. If returns fals, it will not initate any call against to for
     * processing (ie. collEnter, processItem, callExit). But the recursive processing of this item (in case it is
     * Collection) is not affected by this method!
     * 
     * @param item
     * @return
     */
    boolean shouldProcess( StorageItem item );

    /**
     * In case of Collections, StoreWalker will ask should it process those recursively. This is a place to "cut" the
     * tree walking if needed.
     * 
     * @param coll
     * @return
     */
    boolean shouldProcessRecursively( StorageCollectionItem coll );
}
