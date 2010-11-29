package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.HiddenUid;

public class DefaultStoreWalkerFilter
    implements WalkerFilter
{
    public boolean shouldProcess( WalkerContext context, StorageItem item )
    {
        return !isHidden( context, item );
    }

    public boolean shouldProcessRecursively( WalkerContext context, StorageCollectionItem coll )
    {
        return !isHidden( context, coll );
    }

    // ==

    protected boolean isHidden( WalkerContext context, StorageItem item )
    {
        HiddenUid hiddenUid = item.getRepositoryItemUid().getAttribute( HiddenUid.class );

        if ( hiddenUid != null )
        {
            return hiddenUid.getValue();
        }
        else
        {
            return false;
        }
    }
}
