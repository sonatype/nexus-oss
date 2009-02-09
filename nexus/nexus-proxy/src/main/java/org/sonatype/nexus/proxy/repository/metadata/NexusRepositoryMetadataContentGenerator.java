package org.sonatype.nexus.proxy.repository.metadata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = ContentGenerator.class, hint = "NexusRepositoryMetadataContentGenerator" )
public class NexusRepositoryMetadataContentGenerator
    implements ContentGenerator
{
    public ContentLocator generateContent( Repository repository, String path, StorageFileItem item )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        InputStream is = null;

        try
        {
            is = item.getInputStream();

            IOUtil.copy( is, bos );

            String body = new String( bos.toByteArray(), "UTF-8" );

            StringContentLocator result = new StringContentLocator( body.replace( "@rootUrl@", (String) item
                .getItemContext().get( ResourceStoreRequest.CTX_REQUEST_APP_ROOT_URL ) ) );

            item.setLength( result.getByteArray().length );

            return result;
        }
        catch ( IOException e )
        {
            throw new StorageException( e );
        }
        finally
        {
            IOUtil.close( is );
        }
    }
}
