package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A walker processors are units that are "attachable" to a single storage walk, hence the result will be combined but
 * having only one walk. If in any method and exception is thrown, the walker will stop.
 * 
 * @author cstamas
 */
public interface WalkerProcessor
{
    boolean isActive();

    void beforeWalk( WalkerContext context )
        throws Exception;

    void onCollectionEnter( WalkerContext context, StorageCollectionItem coll )
        throws Exception;

    void processItem( WalkerContext context, StorageItem item )
        throws Exception;

    void onCollectionExit( WalkerContext context, StorageCollectionItem coll )
        throws Exception;

    void afterWalk( WalkerContext context )
        throws Exception;
}
