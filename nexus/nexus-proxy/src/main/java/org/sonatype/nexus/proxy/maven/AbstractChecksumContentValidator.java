package org.sonatype.nexus.proxy.maven;

import java.util.Date;
import java.util.List;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

public abstract class AbstractChecksumContentValidator
    extends AbstractLogEnabled
{

    public AbstractChecksumContentValidator()
    {
        super();
    }

    public boolean isRemoteItemContentValid( ProxyRepository proxy, ResourceStoreRequest req, String baseUrl,
                                             AbstractStorageItem item, List<NexusArtifactEvent> events )
        throws StorageException
    {
        ChecksumPolicy checksumPolicy = getChecksumPolicy( proxy, item );
        if ( checksumPolicy == null || !checksumPolicy.shouldCheckChecksum() )
        {
            return true;
        }

        RemoteHashResponse remoteHash = retrieveRemoteHash( item, proxy, baseUrl );

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
        else if ( remoteHash == null )
        {
            msg =
                "Warning, the artifact " + item.getPath() + " has no remote checksum in repository "
                    + item.getRepositoryId() + "!";

            contentValid = true; // policy is STRICT_IF_EXIST or WARN
        }
        else if ( remoteHash.getRemoteHash().equals( retrieveLocalHash( item, remoteHash.getInspector() ) ) )
        {
            // remote hash exists and matches item content
            return true;
        }
        else if ( ChecksumPolicy.WARN.equals( checksumPolicy ) )
        {
            msg =
                "Warning, the artifact " + item.getPath() + " and it's remote checksums does not match in repository "
                    + item.getRepositoryId() + "!";

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

        if ( !contentValid )
        {
            getLogger().debug( "Validation failed due: " + msg );
        }

        events.add( newChechsumFailureEvent( item, msg ) );

        cleanup( proxy, remoteHash, contentValid );

        return contentValid;
    }

    protected String retrieveLocalHash( AbstractStorageItem item, String inspector )
    {
        return item.getAttributes().get( inspector );
    }

    protected abstract void cleanup( ProxyRepository proxy, RemoteHashResponse remoteHash, boolean contentValid )
        throws StorageException;

    protected abstract RemoteHashResponse retrieveRemoteHash( AbstractStorageItem item, ProxyRepository proxy,
                                                              String baseUrl )
        throws StorageException;

    protected abstract ChecksumPolicy getChecksumPolicy( ProxyRepository proxy, AbstractStorageItem item )
        throws StorageException;

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