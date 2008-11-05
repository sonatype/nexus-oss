package org.sonatype.nexus.proxy.walker;

import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

public abstract class AbstractFileWalkerProcessor
    extends AbstractWalkerProcessor
{
    @Override
    public final void processItem( WalkerContext context, StorageItem item )
        throws Exception
    {
        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            processFileItem( context, (StorageFileItem) item );
        }
    }

    /**
     * Process file item.
     * 
     * @param store the store
     * @param fItem the f item
     * @param logger the logger
     */
    protected abstract void processFileItem( WalkerContext context, StorageFileItem fItem )
        throws Exception;
}
