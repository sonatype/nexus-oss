package org.sonatype.nexus.proxy.utils;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * The affirmative implementation of StoraWalkerFilter that is used when no filter is supplied. It will process all
 * items and will dive into all collections.
 * 
 * @author cstamas
 */
public class AffirmativeStoreWalkerFilter
    implements StoreWalkerFilter
{
    public boolean shouldProcess( StorageItem item )
    {
        return true;
    }

    public boolean shouldProcessRecursively( StorageCollectionItem coll )
    {
        return true;
    }
}
