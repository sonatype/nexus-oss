package org.sonatype.nexus.proxy.repository;

import java.io.IOException;
import java.util.Map;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.utils.StoreFileWalker;

public class RecreateAttributesWalker
    extends StoreFileWalker
{
    private Repository repository;

    private Map<String, String> initialData;

    public RecreateAttributesWalker( Repository repository, Logger logger, Map<String, String> initialData )
    {
        super( repository, logger );

        this.repository = repository;

        this.initialData = initialData;
    }

    @Override
    protected void processFileItem( StorageFileItem item )
    {
        try
        {
            if ( getInitialData() != null )
            {
                item.getAttributes().putAll( initialData );
            }

            getRepository().getLocalStorage().getAttributesHandler().storeAttributes(
                (AbstractStorageItem) item,
                item.getInputStream() );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Could not recreate attributes for item " + item.getRepositoryItemUid().toString(), e );
        }
    }

    public Repository getRepository()
    {
        return repository;
    }

    public Map<String, String> getInitialData()
    {
        return initialData;
    }

}
