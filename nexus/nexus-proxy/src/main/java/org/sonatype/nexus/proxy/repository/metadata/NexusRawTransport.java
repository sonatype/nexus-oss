package org.sonatype.nexus.proxy.repository.metadata;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.repository.metadata.RawTransport;
import org.sonatype.nexus.repository.metadata.RawTransportRequest;
import org.sonatype.nexus.util.ContextUtils;

public class NexusRawTransport
    implements RawTransport
{
    private final RepositoryRegistry repositoryRegistry;

    public NexusRawTransport( RepositoryRegistry repositoryRegistry )
    {
        this.repositoryRegistry = repositoryRegistry;
    }

    public byte[] readRawData( RawTransportRequest request )
        throws Exception
    {
        InputStream is = null;

        ByteArrayOutputStream os = null;

        try
        {
            Repository repository = repositoryRegistry.getRepositoryWithFacet( request.getId(), Repository.class );

            HashMap<String, Object> ctx = new HashMap<String, Object>();

            ContextUtils.setFlag( ctx, ResourceStoreRequest.CTX_REMOTE_ONLY_FLAG, true );

            StorageItem item = repository.retrieveItem( repository.createUid( request.getPath() ), ctx );

            if ( item instanceof StorageFileItem )
            {
                StorageFileItem file = (StorageFileItem) item;

                is = file.getInputStream();

                os = new ByteArrayOutputStream();

                IOUtil.copy( is, os );

                return os.toByteArray();
            }
            else
            {
                return null;
            }

        }
        catch ( ItemNotFoundException e )
        {
            // not found should return null
            return null;
        }
        finally
        {
            IOUtil.close( is );

            IOUtil.close( os );
        }
    }
}
