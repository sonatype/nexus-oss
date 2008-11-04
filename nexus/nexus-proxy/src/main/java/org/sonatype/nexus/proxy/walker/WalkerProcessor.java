package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A walker processors are units that are "attachable" to a single storage walk, hence the result will be combined but
 * having only one walk.
 * 
 * @author cstamas
 */
public interface WalkerProcessor
{
    void beforeWalk( WalkerContext context );

    void onCollectionEnter( WalkerContext context, StorageCollectionItem coll );

    void processItem( WalkerContext context, StorageItem item );

    void onCollectionExit( WalkerContext context, StorageCollectionItem coll );

    void afterWalk( WalkerContext context );
}
