package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;

public class ClearCacheWalker
    extends AbstractFileWalkerProcessor
{
    private final Repository repository;

    public ClearCacheWalker( Repository repository )
    {
        this.repository = repository;
    }

    public Repository getRepository()
    {
        return repository;
    }

    @Override
    protected void processFileItem( WalkerContext context, StorageFileItem item )
        throws Exception
    {
        // expiring found files
        try
        {
            // expire it
            item.setExpired( true );

            getRepository().getLocalStorage().updateItemAttributes( getRepository(), null, item );
        }
        catch ( ItemNotFoundException e )
        {
            // will not happen
        }
    }

}
