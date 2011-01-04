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
package org.sonatype.nexus.proxy.target;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerFilter;

/**
 * A Walker filter that will walk only agains a Repository target. Ie. remove snapshots only from Maven target.
 * 
 * @author cstamas
 */
public class TargetStoreWalkerFilter
    implements WalkerFilter
{
    private final Target target;

    public TargetStoreWalkerFilter( Target target )
        throws IllegalArgumentException
    {
        super();

        if ( target == null )
        {
            throw new IllegalArgumentException( "The target cannot be null!" );
        }

        this.target = target;
    }

    public boolean shouldProcess( WalkerContext context, StorageItem item )
    {
        return target.isPathContained( item.getRepositoryItemUid().getRepository().getRepositoryContentClass(), item
            .getPath() );
    }

    public boolean shouldProcessRecursively( WalkerContext context, StorageCollectionItem coll )
    {
        // TODO: initial naive implementation. Later, we could evaluate target patterns: are those "slicing" the repo
        // (ie. forbids /a/b but allows /a and /a/b/c) or "cutting" (ie. allows only /a and nothing below). That would
        // need some pattern magic. We are now naively saying "yes, dive into" but the shouldProcess will do it's work
        // anyway.
        return true;
    }
}
