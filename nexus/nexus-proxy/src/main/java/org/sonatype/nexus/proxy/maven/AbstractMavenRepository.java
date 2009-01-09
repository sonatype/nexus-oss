/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.maven;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.events.RepositoryEventEvictUnusedItems;
import org.sonatype.nexus.proxy.events.RepositoryEventRecreateMavenMetadata;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.EvictUnusedMavenItemsWalkerProcessor.EvictUnusedMavenItemsWalkerFilter;
import org.sonatype.nexus.proxy.repository.ContentValidationResult;
import org.sonatype.nexus.proxy.repository.DefaultRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.MutableProxyRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;

/**
 * The abstract (layout unaware) Maven Repository.
 * 
 * @author cstamas
 */
public abstract class AbstractMavenRepository
    extends DefaultRepository
    implements MavenRepository
{
    /**
     * Metadata manager.
     */
    @Requirement
    private MetadataManager metadataManager;

    private MutableProxyRepositoryKind repositoryKind;

    /** Maven repository policy */
    private RepositoryPolicy repositoryPolicy = RepositoryPolicy.RELEASE;

    /** Should repository metadata be cleaned? */
    private boolean cleanseRepositoryMetadata = false;

    /** Should repository provide correct checksums even if wrong ones are in repo? */
    private boolean fixRepositoryChecksums = false;

    /**
     * The release max age (in minutes).
     */
    private int releaseMaxAge = 24 * 60;

    /**
     * The snapshot max age (in minutes).
     */
    private int snapshotMaxAge = 24 * 60;

    /**
     * The metadata max age (in minutes).
     */
    private int metadataMaxAge = 24 * 60;

    /**
     * Checksum policy applied in this Maven repository.
     */
    private ChecksumPolicy checksumPolicy;

    /**
     * Override the "default" kind with Maven specifics.
     */
    public RepositoryKind getRepositoryKind()
    {
        if ( repositoryKind == null )
        {
            repositoryKind = new MutableProxyRepositoryKind( this, Arrays
                .asList( new Class<?>[] { MavenRepository.class } ), new DefaultRepositoryKind(
                MavenHostedRepository.class,
                null ), new DefaultRepositoryKind( MavenProxyRepository.class, null ) );
        }

        return repositoryKind;
    }

    public Collection<String> evictUnusedItems( final long timestamp )
    {
        EvictUnusedMavenItemsWalkerProcessor walkerProcessor = new EvictUnusedMavenItemsWalkerProcessor( timestamp );

        DefaultWalkerContext ctx = new DefaultWalkerContext( this, new EvictUnusedMavenItemsWalkerFilter() );

        ctx.getProcessors().add( walkerProcessor );

        getWalker().walk( ctx );

        notifyProximityEventListeners( new RepositoryEventEvictUnusedItems( this ) );

        return walkerProcessor.getFiles();
    }

    public boolean recreateMavenMetadata( String path )
    {
        getLogger().info( "Recreating Maven medadata on repository " + getId() );

        RecreateMavenMetadataWalkerProcessor wp = new RecreateMavenMetadataWalkerProcessor();

        DefaultWalkerContext ctx = new DefaultWalkerContext( this );

        ctx.getProcessors().add( wp );

        getWalker().walk( ctx, path );

        notifyProximityEventListeners( new RepositoryEventRecreateMavenMetadata( this ) );

        return !ctx.isStopped();
    }

    public RepositoryPolicy getRepositoryPolicy()
    {
        return repositoryPolicy;
    }

    public void setRepositoryPolicy( RepositoryPolicy repositoryPolicy )
    {
        this.repositoryPolicy = repositoryPolicy;
    }

    public boolean isCleanseRepositoryMetadata()
    {
        return cleanseRepositoryMetadata;
    }

    public void setCleanseRepositoryMetadata( boolean cleanseRepositoryMetadata )
    {
        this.cleanseRepositoryMetadata = cleanseRepositoryMetadata;
    }

    public boolean isFixRepositoryChecksums()
    {
        return fixRepositoryChecksums;
    }

    public void setFixRepositoryChecksums( boolean fixRepositoryChecksums )
    {
        this.fixRepositoryChecksums = fixRepositoryChecksums;
    }

    public ChecksumPolicy getChecksumPolicy()
    {
        return checksumPolicy;
    }

    public void setChecksumPolicy( ChecksumPolicy checksumPolicy )
    {
        this.checksumPolicy = checksumPolicy;
    }

    public int getReleaseMaxAge()
    {
        return releaseMaxAge;
    }

    public void setReleaseMaxAge( int releaseMaxAge )
    {
        this.releaseMaxAge = releaseMaxAge;
    }

    public int getSnapshotMaxAge()
    {
        return snapshotMaxAge;
    }

    public void setSnapshotMaxAge( int snapshotMaxAge )
    {
        this.snapshotMaxAge = snapshotMaxAge;
    }

    public int getMetadataMaxAge()
    {
        return metadataMaxAge;
    }

    public void setMetadataMaxAge( int metadataMaxAge )
    {
        this.metadataMaxAge = metadataMaxAge;
    }

    public abstract boolean shouldServeByPolicies( RepositoryItemUid uid );

    public void storeItemWithChecksums( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "storeItemWithChecksums() :: " + request.getRequestPath() );
        }

        try
        {
            try
            {
                storeItem( request, is, userAttributes );
            }
            catch ( IOException e )
            {
                throw new StorageException( "Could not get the content from the ContentLocator!", e );
            }

            RepositoryItemUid itemUid = createUid( request.getRequestPath() );

            StorageFileItem storedFile = (StorageFileItem) retrieveItem( itemUid, null );

            String sha1Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );

            String md5Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY );

            if ( !StringUtils.isEmpty( sha1Hash ) )
            {
                storeItem( new DefaultStorageFileItem(
                    this,
                    storedFile.getPath() + ".sha1",
                    true,
                    true,
                    new StringContentLocator( sha1Hash ) ) );
            }

            if ( !StringUtils.isEmpty( md5Hash ) )
            {
                storeItem( new DefaultStorageFileItem(
                    this,
                    storedFile.getPath() + ".md5",
                    true,
                    true,
                    new StringContentLocator( md5Hash ) ) );
            }
        }
        catch ( ItemNotFoundException e )
        {
            throw new StorageException( "Storage inconsistency!", e );
        }
    }

    public void deleteItemWithChecksums( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "deleteItemWithChecksums() :: " + request.getRequestPath() );
        }

        try
        {
            deleteItem( request );
        }
        catch ( ItemNotFoundException e )
        {
            if ( request.getRequestPath().endsWith( ".asc" ) )
            {
                // Do nothing no guarantee that the .asc files will exist
            }
            else
            {
                throw e;
            }
        }

        String originalPath = request.getRequestPath();

        request.setRequestPath( originalPath + ".sha1" );

        try
        {
            deleteItem( request );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }

        request.setRequestPath( originalPath + ".md5" );

        try
        {
            deleteItem( request );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }

        // Now remove the .asc files, and the checksums stored with them as well
        // Note this is a recursive call, hence the check for .asc
        if ( !originalPath.endsWith( ".asc" ) )
        {
            request.setRequestPath( originalPath + ".asc" );

            deleteItemWithChecksums( request );
        }
    }

    public void storeItemWithChecksums( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "storeItemWithChecksums() :: " + item.getRepositoryItemUid().toString() );
        }

        try
        {
            try
            {
                storeItem( item );
            }
            catch ( IOException e )
            {
                throw new StorageException( "Could not get the content from the ContentLocator!", e );
            }

            StorageFileItem storedFile = (StorageFileItem) retrieveItem( item.getRepositoryItemUid(), item
                .getItemContext() );

            String sha1Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );

            String md5Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY );

            if ( !StringUtils.isEmpty( sha1Hash ) )
            {
                storeItem( new DefaultStorageFileItem(
                    this,
                    item.getPath() + ".sha1",
                    true,
                    true,
                    new StringContentLocator( sha1Hash ) ) );
            }

            if ( !StringUtils.isEmpty( md5Hash ) )
            {
                storeItem( new DefaultStorageFileItem(
                    this,
                    item.getPath() + ".md5",
                    true,
                    true,
                    new StringContentLocator( md5Hash ) ) );
            }
        }
        catch ( ItemNotFoundException e )
        {
            throw new StorageException( "Storage inconsistency!", e );
        }
    }

    public void deleteItemWithChecksums( RepositoryItemUid uid, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "deleteItemWithChecksums() :: " + uid.toString() );
        }

        try
        {
            deleteItem( uid, context );
        }
        catch ( ItemNotFoundException e )
        {
            if ( uid.getPath().endsWith( ".asc" ) )
            {
                // Do nothing no guarantee that the .asc files will exist
            }
            else
            {
                throw e;
            }
        }

        RepositoryItemUid sha1Uid = createUid( uid.getPath() + ".sha1" );

        try
        {
            deleteItem( sha1Uid, context );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }

        RepositoryItemUid md5Uid = createUid( uid.getPath() + ".md5" );

        try
        {
            deleteItem( md5Uid, context );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }

        // Now remove the .asc files, and the checksums stored with them as well
        // Note this is a recursive call, hence the check for .asc
        if ( !uid.getPath().endsWith( ".asc" ) )
        {
            deleteItemWithChecksums( createUid( uid.getPath() + ".asc" ), context );
        }
    }

    public MetadataManager getMetadataManager()
    {
        return metadataManager;
    }

    // =================================================================================
    // DefaultRepository customizations

    @Override
    protected StorageItem doRetrieveItem( RepositoryItemUid uid, Map<String, Object> context )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        if ( !shouldServeByPolicies( uid ) )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "The serving of item " + uid.toString() + " is forbidden by Maven repository policy." );
            }
            throw new ItemNotFoundException( uid );
        }

        return super.doRetrieveItem( uid, context );
    }

    @Override
    public void storeItem( StorageItem item )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException
    {
        if ( shouldServeByPolicies( item.getRepositoryItemUid() ) )
        {
            super.storeItem( item );
        }
        else
        {
            String msg = "Storing of item " + item.getRepositoryItemUid().toString()
                + " is forbidden by Maven Repository policy. Because " + getId() + " is a "
                + getRepositoryPolicy().name() + " repository";

            getLogger().info( msg );

            throw new UnsupportedStorageOperationException( msg );
        }
    }

    @Override
    public boolean isCompatible( Repository repository )
    {
        if ( super.isCompatible( repository ) && MavenRepository.class.isAssignableFrom( repository.getClass() )
            && getRepositoryPolicy().equals( ( (MavenRepository) repository ).getRepositoryPolicy() ) )
        {
            return true;
        }

        return false;
    }

    // =================================================================================
    // DefaultRepository customizations

    @Override
    protected AbstractStorageItem doRetrieveRemoteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            RemoteAccessException,
            StorageException
    {
        if ( !isChecksum( uid ) )
        {
            // we are about to download an artifact from remote repository
            // lets clean any existing (stale) checksum files
            removeLocalChecksum( uid, context );
        }

        return super.doRetrieveRemoteItem( uid, context );
    }

    @Override
    protected ContentValidationResult doValidateRemoteItemContent( String baseUrl, AbstractStorageItem item,
        Map<String, Object> context )
        throws RemoteAccessException,
            StorageException
    {
        if ( isChecksum( item.getRepositoryItemUid() ) )
        {
            // do not validate checksum files
            return null;
        }

        if ( getChecksumPolicy() == null || !getChecksumPolicy().shouldCheckChecksum()
            || !( item instanceof DefaultStorageFileItem ) )
        {
            // there is either no need to validate or we can't validate the item content
            return null;
        }

        RepositoryItemUid uid = item.getRepositoryItemUid();

        DefaultStorageFileItem hashItem = null;

        // we prefer SHA1 ...
        try
        {
            String path = uid.getRepository().createUid( uid.getPath() + ".sha1" ).getPath();

            hashItem = (DefaultStorageFileItem) getRemoteStorage().retrieveItem( this, context, getRemoteUrl(), path );
        }
        catch ( ItemNotFoundException sha1e )
        {
            // ... but MD5 will do too
            try
            {
                String path = uid.getRepository().createUid( uid.getPath() + ".md5" ).getPath();

                hashItem = (DefaultStorageFileItem) getRemoteStorage().retrieveItem(
                    this,
                    context,
                    getRemoteUrl(),
                    path );
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
            hashItem = (DefaultStorageFileItem) doCacheItem( hashItem );

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

        ContentValidationResult result = new ContentValidationResult();

        if ( remoteHash == null && ChecksumPolicy.STRICT.equals( getChecksumPolicy() ) )
        {
            msg = "The artifact " + item.getPath() + " has no remote checksum in repository " + item.getRepositoryId()
                + "! The checksumPolicy of repository forbids downloading of it.";

            contentValid = false;
        }
        else if ( hashItem == null )
        {
            msg = "Warning, the artifact " + item.getPath() + " has no remote checksum in repository "
                + item.getRepositoryId() + "!";

            contentValid = true; // policy is STRICT_IF_EXIST or WARN
        }
        else
        {
            String hashKey = hashItem.getPath().endsWith( ".sha1" )
                ? DigestCalculatingInspector.DIGEST_SHA1_KEY
                : DigestCalculatingInspector.DIGEST_MD5_KEY;

            if ( remoteHash != null && remoteHash.equals( item.getAttributes().get( hashKey ) ) )
            {
                // remote hash exists and matches item content
                return null;
            }

            if ( ChecksumPolicy.WARN.equals( getChecksumPolicy() ) )
            {
                msg = "Warning, the artifact " + item.getPath()
                    + " and it's remote checksums does not match in repository " + item.getRepositoryId() + "!";

                contentValid = true;
            }
            else
            // STRICT or STRICT_IF_EXISTS
            {
                msg = "The artifact " + item.getPath() + " and it's remote checksums does not match in repository "
                    + item.getRepositoryId() + "! The checksumPolicy of repository forbids downloading of it.";

                contentValid = false;
            }
        }

        result.addEvent( newChechsumFailureEvent( item, msg ) );
        result.setContentValid( contentValid );

        if ( !contentValid && hashItem != null )
        {
            // TODO should we remove bad checksum if policy==WARN?
            try
            {
                getLocalStorage().deleteItem( this, context, hashItem.getRepositoryItemUid().getPath() );
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

        return result;
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

    private boolean isChecksum( RepositoryItemUid uid )
    {
        return uid.getPath().endsWith( ".sha1" ) || uid.getPath().endsWith( ".md5" );
    }

    private void removeLocalChecksum( RepositoryItemUid uid, Map<String, Object> context )
        throws StorageException
    {
        try
        {
            try
            {
                getLocalStorage().deleteItem( this, context, uid.getPath() + ".sha1" );
            }
            catch ( ItemNotFoundException e )
            {
                // this is exactly what we're trying to achieve
            }

            try
            {
                getLocalStorage().deleteItem( this, context, uid.getPath() + ".md5" );
            }
            catch ( ItemNotFoundException e )
            {
                // this is exactly what we're trying to achieve
            }
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // huh?
        }

    }

    protected void markItemRemotelyChecked( RepositoryItemUid uid, Map<String, Object> context )
        throws StorageException,
            ItemNotFoundException
    {
        super.markItemRemotelyChecked( uid, context );

        RepositoryItemUid sha1Uid = uid.getRepository().createUid( uid.getPath() + ".sha1" );

        if ( getLocalStorage().containsItem( this, context, sha1Uid.getPath() ) )
        {
            super.markItemRemotelyChecked( sha1Uid, context );
        }

        RepositoryItemUid md5Uid = uid.getRepository().createUid( uid.getPath() + ".md5" );

        if ( getLocalStorage().containsItem( this, context, md5Uid.getPath() ) )
        {
            super.markItemRemotelyChecked( md5Uid, context );
        }
    }
    
    public void deleteItem( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        RepositoryItemUid uid = createUid( request.getRequestPath() );

        Map<String, Object> context = request.getRequestContext();

        // first determine from where to rebuild metadata
        String path = RepositoryItemUid.PATH_ROOT;

        StorageItem item = this.retrieveItem( uid, context );

        if ( item instanceof StorageCollectionItem )
        {
            path = getParentPath( item.getPath() );
        }
        else if ( item instanceof StorageFileItem )
        {
            path = getParentPath( getParentPath( item.getPath() ) );
        }

        // then delete the item
        super.deleteItem( request );

        // finally rebuild metadata
        recreateMavenMetadata( path );
    }

    // TODO: maybe it's better to move this method to RepositoryItemUid
    private String getParentPath( String path )
    {
        if ( RepositoryItemUid.PATH_ROOT.equals( path ) )
        {
            return path;
        }

        int lastSepratorPos = path.lastIndexOf( RepositoryItemUid.PATH_SEPARATOR );

        if ( lastSepratorPos == 1 )
        {
            return RepositoryItemUid.PATH_ROOT;
        }
        else
        {
            return path.substring( 0, lastSepratorPos );
        }
    }
}
