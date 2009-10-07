package org.sonatype.nexus.proxy.target;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerFilter;

/**
 * A Walker filter that will walk only agains a Repository target. Ie. remove snapshots only from Maven target.
 * 
 * @author cstamas
 */
public class TargetStoreWalkerFilter
    implements WalkerFilter
{
    private final Target target;

    public TargetStoreWalkerFilter( Target target )
        throws IllegalArgumentException
    {
        super();

        if ( target == null )
        {
            throw new IllegalArgumentException( "The target cannot be null!" );
        }

        this.target = target;
    }

    public boolean shouldProcess( WalkerContext context, StorageItem item )
    {
        return target.isPathContained( item.getRepositoryItemUid().getRepository().getRepositoryContentClass(), item
            .getPath() );
    }

    public boolean shouldProcessRecursively( WalkerContext context, StorageCollectionItem coll )
    {
        // TODO: initial naive implementation. Later, we could evaluate target patterns: are those "slicing" the repo
        // (ie. forbids /a/b but allows /a and /a/b/c) or "cutting" (ie. allows only /a and nothing below). That would
        // need some pattern magic. We are now naively saying "yes, dive into" but the shouldProcess will do it's work
        // anyway.
        return true;
    }
}
