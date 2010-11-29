package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenUidAttribute;

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
        Boolean isHidden = item.getRepositoryItemUid().getAttributeValue( IsHiddenUidAttribute.class );

        if ( isHidden != null )
        {
            return isHidden.booleanValue();
        }
        else
        {
            return false;
        }
    }
}
