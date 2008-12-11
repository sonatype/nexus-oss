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

public abstract class AbstractWalkerProcessor
    implements WalkerProcessor
{
    private boolean active = true;

    public boolean isActive()
    {
        return active;
    }

    public void setActive( boolean active )
    {
        this.active = active;
    }

    public void beforeWalk( WalkerContext context )
        throws Exception
    {
    }

    public void onCollectionEnter( WalkerContext context, StorageCollectionItem coll )
        throws Exception
    {
    }

    public abstract void processItem( WalkerContext context, StorageItem item )
        throws Exception;

    public void onCollectionExit( WalkerContext context, StorageCollectionItem coll )
        throws Exception
    {
    }

    public void afterWalk( WalkerContext context )
        throws Exception
    {
    }
}
