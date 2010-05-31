package org.sonatype.nexus.proxy.maven;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.ItemContentValidator;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * Maven checksum content validator.
 * 
 * @author cstamas
 */
@Component( role = ItemContentValidator.class, hint = "ChecksumContentValidator" )
public class ChecksumContentValidator
    extends AbstractChecksumContentValidator
    implements ItemContentValidator
{

    @Override
    protected void cleanup( ProxyRepository proxy, RemoteHashResponse remoteHash, boolean contentValid )
        throws StorageException
    {
        if ( !contentValid && remoteHash.getHashItem() != null )
        {
            // TODO should we remove bad checksum if policy==WARN?
            try
            {
                proxy.getLocalStorage().deleteItem(
                                                    proxy,
                                                    new ResourceStoreRequest(
                                                                              remoteHash.getHashItem().getRepositoryItemUid().getPath(),
                                                                              true ) );
            }
            catch ( ItemNotFoundException e )
            {
                // ignore
            }
            catch ( UnsupportedStorageOperationException e )
            {
                // huh?
            }
        }
    }

    @Override
    protected ChecksumPolicy getChecksumPolicy( ProxyRepository proxy, AbstractStorageItem item )
    {
        if ( isChecksum( item.getRepositoryItemUid().getPath() ) )
        {
            // do not validate checksum files
            return null;
        }

        if ( !proxy.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
        {
            // we work only with maven proxy reposes, all others are neglected
            return null;
        }

        MavenProxyRepository mpr = proxy.adaptToFacet( MavenProxyRepository.class );

        ChecksumPolicy checksumPolicy = mpr.getChecksumPolicy();

        if ( checksumPolicy == null || !checksumPolicy.shouldCheckChecksum()
            || !( item instanceof DefaultStorageFileItem ) )
        {
            // there is either no need to validate or we can't validate the item content
            return null;
        }

        return checksumPolicy;
    }

    @Override
    protected RemoteHashResponse retrieveRemoteHash( AbstractStorageItem item, ProxyRepository proxy, String baseUrl )
        throws StorageException
    {
        RepositoryItemUid uid = item.getRepositoryItemUid();

        ResourceStoreRequest request = new ResourceStoreRequest( item );

        DefaultStorageFileItem hashItem = null;

        String inspector;
        // we prefer SHA1 ...
        try
        {
            inspector = DigestCalculatingInspector.DIGEST_SHA1_KEY;
            request.pushRequestPath( uid.getPath() + ".sha1" );

            hashItem = doRetriveRemoteChecksumItem( proxy, request );
        }
        catch ( ItemNotFoundException sha1e )
        {
            // ... but MD5 will do too
            inspector = DigestCalculatingInspector.DIGEST_MD5_KEY;
            try
            {
                request.popRequestPath();

                request.pushRequestPath( uid.getPath() + ".md5" );

                hashItem = doRetriveRemoteChecksumItem( proxy, request );
            }
            catch ( ItemNotFoundException md5e )
            {
                getLogger().debug( "Item checksums (SHA1, MD5) remotely unavailable " + uid.toString() );
            }
        }

        String remoteHash = null;

        if ( hashItem != null )
        {
            // store checksum file locally
            hashItem = (DefaultStorageFileItem) proxy.doCacheItem( hashItem );

            // read checksum
            try
            {
                InputStream hashItemContent = hashItem.getInputStream();

                try
                {
                    remoteHash = StringUtils.chomp( IOUtil.toString( hashItemContent ) ).trim().split( " " )[0];
                }
                finally
                {
                    IOUtil.close( hashItemContent );
                }
            }
            catch ( IOException e )
            {
                getLogger().warn( "Cannot read hash string for remotely fetched StorageFileItem: " + uid.toString(), e );
            }
        }

        if ( remoteHash == null )
        {
            return null;
        }

        return new RemoteHashResponse( inspector, remoteHash, hashItem );
    }

    private boolean isChecksum( String path )
    {
        return path.endsWith( ".sha1" ) || path.endsWith( ".md5" );
    }

    private DefaultStorageFileItem doRetriveRemoteChecksumItem( ProxyRepository proxy, ResourceStoreRequest request )
        throws ItemNotFoundException
    {
        try
        {
            return (DefaultStorageFileItem) proxy.getRemoteStorage().retrieveItem( proxy, request, proxy.getRemoteUrl() );
        }
        catch ( RemoteAccessException e )
        {
            throw new ItemNotFoundException( request, proxy, e );
        }
        catch ( StorageException e )
        {
            throw new ItemNotFoundException( request, proxy, e );
        }
    }
}
