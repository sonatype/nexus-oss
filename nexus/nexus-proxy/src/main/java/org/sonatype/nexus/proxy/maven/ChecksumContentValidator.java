package org.sonatype.nexus.proxy.maven;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
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
    extends AbstractLogEnabled
    implements ItemContentValidator
{

    public boolean isRemoteItemContentValid( ProxyRepository proxy, ResourceStoreRequest req, String baseUrl,
                                             AbstractStorageItem item, List<NexusArtifactEvent> events )
        throws StorageException
    {
        if ( isChecksum( item.getRepositoryItemUid().getPath() ) )
        {
            // do not validate checksum files
            return true;
        }

        if ( !proxy.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
        {
            // we work only with maven proxy reposes, all others are neglected
            return true;
        }

        MavenProxyRepository mpr = proxy.adaptToFacet( MavenProxyRepository.class );

        ChecksumPolicy checksumPolicy = mpr.getChecksumPolicy();

        if ( checksumPolicy == null || !checksumPolicy.shouldCheckChecksum()
            || !( item instanceof DefaultStorageFileItem ) )
        {
            // there is either no need to validate or we can't validate the item content
            return true;
        }

        RepositoryItemUid uid = item.getRepositoryItemUid();

        ResourceStoreRequest request = new ResourceStoreRequest( item );

        DefaultStorageFileItem hashItem = null;

        // we prefer SHA1 ...
        try
        {
            request.pushRequestPath( uid.getPath() + ".sha1" );

            hashItem = doRetriveRemoteChecksumItem( proxy, request );
        }
        catch ( ItemNotFoundException sha1e )
        {
            // ... but MD5 will do too
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

        // let compiler make sure I did not forget to populate validation results
        String msg;
        boolean contentValid;

        if ( remoteHash == null && ChecksumPolicy.STRICT.equals( checksumPolicy ) )
        {
            msg =
                "The artifact " + item.getPath() + " has no remote checksum in repository " + item.getRepositoryId()
                    + "! The checksumPolicy of repository forbids downloading of it.";

            contentValid = false;
        }
        else if ( hashItem == null )
        {
            msg =
                "Warning, the artifact " + item.getPath() + " has no remote checksum in repository "
                    + item.getRepositoryId() + "!";

            contentValid = true; // policy is STRICT_IF_EXIST or WARN
        }
        else
        {
            String hashKey =
                hashItem.getPath().endsWith( ".sha1" ) ? DigestCalculatingInspector.DIGEST_SHA1_KEY
                                : DigestCalculatingInspector.DIGEST_MD5_KEY;

            if ( remoteHash != null && remoteHash.equals( item.getAttributes().get( hashKey ) ) )
            {
                // remote hash exists and matches item content
                return true;
            }

            if ( ChecksumPolicy.WARN.equals( checksumPolicy ) )
            {
                msg =
                    "Warning, the artifact " + item.getPath()
                        + " and it's remote checksums does not match in repository " + item.getRepositoryId() + "!";

                contentValid = true;
            }
            else
            // STRICT or STRICT_IF_EXISTS
            {
                msg =
                    "The artifact " + item.getPath() + " and it's remote checksums does not match in repository "
                        + item.getRepositoryId() + "! The checksumPolicy of repository forbids downloading of it.";

                contentValid = false;
            }
        }

        events.add( newChechsumFailureEvent( item, msg ) );

        if ( !contentValid && hashItem != null )
        {
            // TODO should we remove bad checksum if policy==WARN?
            try
            {
                proxy.getLocalStorage().deleteItem(
                                                    proxy,
                                                    new ResourceStoreRequest(
                                                                              hashItem.getRepositoryItemUid().getPath(),
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

        return contentValid;
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

    private NexusArtifactEvent newChechsumFailureEvent( AbstractStorageItem item, String msg )
    {
        NexusArtifactEvent nae = new NexusArtifactEvent();

        nae.setAction( NexusArtifactEvent.ACTION_BROKEN_WRONG_REMOTE_CHECKSUM );

        nae.setEventDate( new Date() );

        nae.setEventContext( item.getItemContext() );

        nae.setMessage( msg );

        NexusItemInfo ai = new NexusItemInfo();

        ai.setPath( item.getPath() );

        ai.setRepositoryId( item.getRepositoryId() );

        ai.setRemoteUrl( item.getRemoteUrl() );

        nae.setNexusItemInfo( ai );

        return nae;
    }
}
