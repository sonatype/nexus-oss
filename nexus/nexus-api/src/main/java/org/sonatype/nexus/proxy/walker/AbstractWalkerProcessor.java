package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

public abstract class AbstractWalkerProcessor
    implements WalkerProcessor
{
    public void beforeWalk( WalkerContext context )
    {
    }

    public void onCollectionEnter( WalkerContext context, StorageCollectionItem coll )
    {
    }

    public abstract void processItem( WalkerContext context, StorageItem item );

    public void onCollectionExit( WalkerContext context, StorageCollectionItem coll )
    {
    }

    public void afterWalk( WalkerContext context )
    {
    }
}
