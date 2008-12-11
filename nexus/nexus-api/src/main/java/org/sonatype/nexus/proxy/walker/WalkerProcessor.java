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
package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A walker processors are units that are "attachable" to a single storage walk, hence the result will be combined but
 * having only one walk. If in any method and exception is thrown, the walker will stop.
 * 
 * @author cstamas
 */
public interface WalkerProcessor
{
    boolean isActive();

    void beforeWalk( WalkerContext context )
        throws Exception;

    void onCollectionEnter( WalkerContext context, StorageCollectionItem coll )
        throws Exception;

    void processItem( WalkerContext context, StorageItem item )
        throws Exception;

    void onCollectionExit( WalkerContext context, StorageCollectionItem coll )
        throws Exception;

    void afterWalk( WalkerContext context )
        throws Exception;
}
