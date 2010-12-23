package org.sonatype.nexus.proxy.wastebasket;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerProcessor;

public class WastebasketWalker
    extends AbstractWalkerProcessor
    implements WalkerProcessor
{

    private long age;

    public WastebasketWalker( long age )
    {
        this.age = age;
    }

    @Override
    public void processItem( WalkerContext ctx, StorageItem item )
        throws Exception
    {
        long now = System.currentTimeMillis();
        long limitDate = now - age;

        if ( item instanceof StorageFileItem && item.getModified() < limitDate )
        {
            try
            {
                ctx.getRepository().getLocalStorage().shredItem( ctx.getRepository(), item.getResourceStoreRequest() );
            }
            catch ( ItemNotFoundException e )
            {
                // silent
            }
            catch ( UnsupportedStorageOperationException e )
            {
                // silent?
            }
        }
    }

}
