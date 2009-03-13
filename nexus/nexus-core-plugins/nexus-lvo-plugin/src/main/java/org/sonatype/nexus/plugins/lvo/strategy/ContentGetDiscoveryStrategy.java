package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryRequest;

/**
 * This is a "local" strategy, uses Nexus content for information fetch.
 * 
 * @author cstamas
 */
@Component( role = DiscoveryStrategy.class, hint = "content-get" )
public class ContentGetDiscoveryStrategy
    extends AbstractDiscoveryStrategy
{
    public DiscoveryResponse discoverLatestVersion( DiscoveryRequest request )
        throws NoSuchRepositoryException,
            IOException
    {
        DiscoveryResponse dr = new DiscoveryResponse( request );

        // handle
        StorageFileItem response = handleRequest( request );

        if ( response != null )
        {
            InputStream is = response.getInputStream();

            try
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                IOUtil.copy( is, bos );

                dr.setVersion( bos.toString() );

                dr.setSuccessful( true );
            }
            finally
            {
                IOUtil.close( is );
            }
        }

        return dr;
    }

    protected StorageFileItem handleRequest( DiscoveryRequest request )
    {
        try
        {
            // NoSuchRepository if the repoId is not known
            Repository repository = getNexus().getRepository( request.getLvoKey().getRepositoryId() );

            // ItemNotFound if the path does not exists
            StorageItem item = repository.retrieveItem( new RepositoryRequest( repository, new ResourceStoreRequest(
                request.getLvoKey().getLocalPath(),
                false ) ) );

            // return only if item is a file, nuke it otherwise
            if ( item instanceof StorageFileItem )
            {
                return (StorageFileItem) item;
            }
            else
            {
                return null;
            }
        }
        catch ( Exception e )
        {
            // we are very rude about exceptions here ;)
            getLogger().warn( "Could not retrieve content!", e );

            return null;
        }
    }
}
