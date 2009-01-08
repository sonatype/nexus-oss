package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A simple walkerFilter that filters out all items (directories and files) that starts with dot ('.'), aka "hidden"
 * files.
 * 
 * @author cstamas
 */
public class DottedStoreWalkerFilter
    implements WalkerFilter
{
    public boolean shouldProcess( WalkerContext ctx, StorageItem item )
    {
        return shouldProcessItem( item );
    }

    public boolean shouldProcessRecursively( WalkerContext ctx, StorageCollectionItem coll )
    {
        return shouldProcessItem( coll );
    }

    protected boolean shouldProcessItem( StorageItem item )
    {
        return !item.getName().startsWith( "." );
    }
}
