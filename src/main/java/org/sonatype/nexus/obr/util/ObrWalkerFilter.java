/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.obr.util;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerFilter;

/**
 * {@link WalkerFilter} that only accepts files that are potential OBR bundle resource candidates.
 */
public class ObrWalkerFilter
    implements WalkerFilter
{
    public boolean shouldProcess( WalkerContext context, StorageItem item )
    {
        return ObrUtils.acceptItem( item );
    }

    public boolean shouldProcessRecursively( WalkerContext context, StorageCollectionItem coll )
    {
        return !coll.getName().startsWith( "." );
    }
}
