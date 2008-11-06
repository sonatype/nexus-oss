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
