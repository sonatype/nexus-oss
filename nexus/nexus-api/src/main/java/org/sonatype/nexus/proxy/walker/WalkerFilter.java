package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A simple filter a la FileFilter, for filtering which items should be processed. Note: the StoreWalker does RECURSIVE
 * processing, hence unlike FileFilter, if the current collection is filtered out, but the filter says diveIn, it will
 * dive in deeper.
 * 
 * @author cstamas
 */
public interface WalkerFilter
{
    /**
     * This method is called for every item implementation. If returns fals, it will not initate any call against to for
     * processing (ie. collEnter, processItem, callExit). But the recursive processing of this item (in case it is
     * Collection) is not affected by this method!
     * 
     * @param context
     * @param item
     * @return
     */
    boolean shouldProcess( WalkerContext context, StorageItem item );

    /**
     * In case of Collections, StoreWalker will ask should it process those recursively. This is a place to "cut" the
     * tree walking if needed.
     * 
     * @paramt context
     * @param coll
     * @return
     */
    boolean shouldProcessRecursively( WalkerContext context, StorageCollectionItem coll );
}
