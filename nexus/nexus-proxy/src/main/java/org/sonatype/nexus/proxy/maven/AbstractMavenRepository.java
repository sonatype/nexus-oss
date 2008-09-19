/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.maven;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.events.RepositoryEventEvictUnusedItems;
import org.sonatype.nexus.proxy.events.RepositoryEventRecreateAttributes;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.DefaultRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

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
     * Feed recorder.
     * 
     * @plexus.requirement
     */
    private FeedRecorder feedRecorder;

    /**
     * Metadata manager.
     * 
     * @plexus.requirement
     */
    private MetadataManager metadataManager;

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
     * ArtifactStoreHelper.
     */
    private ArtifactStoreHelper artifactStoreHelper;

    public Collection<String> evictUnusedItems( final long timestamp )
    {
        EvictUnusedMavenItemsWalker walker = new EvictUnusedMavenItemsWalker( this, getLogger(), timestamp );

        // and let it loose
        walker.walk( RepositoryItemUid.PATH_ROOT, true, false );

        notifyProximityEventListeners( new RepositoryEventEvictUnusedItems( this ) );

        return walker.getFiles();
    }

    public boolean recreateAttributes( String path, final Map<String, String> initialData )
    {
        getLogger().info( "Recreating Maven attributes on repository " + getId() );

        RecreateMavenAttributesWalker walker = new RecreateMavenAttributesWalker( this, getLogger(), initialData );

        walker.walk( path, true, false );

        notifyProximityEventListeners( new RepositoryEventRecreateAttributes( this ) );

        return true;
    }

    public ChecksumPolicy getChecksumPolicy()
    {
        return checksumPolicy;
    }

    public void setChecksumPolicy( ChecksumPolicy checksumPolicy )
    {
        this.checksumPolicy = checksumPolicy;
    }

    public RepositoryPolicy getRepositoryPolicy()
    {
        return repositoryPolicy;
    }

    public void setRepositoryPolicy( RepositoryPolicy repositoryPolicy )
    {
        this.repositoryPolicy = repositoryPolicy;
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

    public FeedRecorder getFeedRecorder()
    {
        return feedRecorder;
    }

    public void storeItemWithChecksums( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
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

            StorageFileItem storedFile = (StorageFileItem) retrieveItem( true, item.getRepositoryItemUid(), item
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
            RepositoryNotAvailableException,
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

    public void setFeedRecorder( FeedRecorder feedRecorder )
    {
        this.feedRecorder = feedRecorder;
    }

    public MetadataManager getMetadataManager()
    {
        return metadataManager;
    }

    protected ArtifactStoreHelper getArtifactStoreHelper()
    {
        if ( artifactStoreHelper == null )
        {
            artifactStoreHelper = new ArtifactStoreHelper( this );
        }
        return artifactStoreHelper;
    }

    public abstract boolean shouldServeByPolicies( RepositoryItemUid uid );

    // =================================================================================
    // ArtifactStore iface

    public StorageFileItem retrieveArtifactPom( ArtifactStoreRequest gavRequest )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        return getArtifactStoreHelper().retrieveArtifactPom( gavRequest );
    }

    public StorageFileItem retrieveArtifact( ArtifactStoreRequest gavRequest )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        return getArtifactStoreHelper().retrieveArtifact( gavRequest );
    }

    public void storeArtifact( ArtifactStoreRequest gavRequest, InputStream is, Map<String, String> attributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        getArtifactStoreHelper().storeArtifact( gavRequest, is, attributes );
    }

    public void storeArtifactPom( ArtifactStoreRequest gavRequest, InputStream is, Map<String, String> attributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        getArtifactStoreHelper().storeArtifactPom( gavRequest, is, attributes );
    }

    public void storeArtifactWithGeneratedPom( ArtifactStoreRequest gavRequest, InputStream is,
        Map<String, String> attributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        getArtifactStoreHelper().storeArtifactWithGeneratedPom( gavRequest, is, attributes );
    }

    public void deleteArtifactPom( ArtifactStoreRequest gavRequest, boolean withChecksums, boolean withAllSubordinates,
        boolean deleteWholeGav )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        getArtifactStoreHelper().deleteArtifactPom( gavRequest, withChecksums, withAllSubordinates, deleteWholeGav );
    }

    public void deleteArtifact( ArtifactStoreRequest gavRequest, boolean withChecksums, boolean withAllSubordinates,
        boolean deleteWholeGav )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        getArtifactStoreHelper().deleteArtifact( gavRequest, withChecksums, withAllSubordinates, deleteWholeGav );
    }

    public Collection<Gav> listArtifacts( ArtifactStoreRequest gavRequest )
    {
        return getArtifactStoreHelper().listArtifacts( gavRequest );
    }

    // =================================================================================
    // DefaultRepository customizations

    protected StorageItem doRetrieveItem( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        if ( shouldServeByPolicies( uid ) )
        {
            boolean isChecksum = uid.getPath().endsWith( ".sha1" ) || uid.getPath().endsWith( ".md5" );

            if ( !isFixRepositoryChecksums() || !isChecksum )
            {
                // the "normal" way, serving the file from repo (cache or remote, whatever)
                return super.doRetrieveItem( localOnly, uid, context );
            }
            else
            {
                // otherwise we get the "owner" (who's checksum is this) and simply
                // create a File item with prepared content: the hash
                String ownerPath = null;

                if ( uid.getPath().endsWith( ".sha1" ) )
                {
                    ownerPath = uid.getPath().substring( 0, uid.getPath().length() - 5 );
                }
                else
                {
                    ownerPath = uid.getPath().substring( 0, uid.getPath().length() - 4 );
                }

                RepositoryItemUid ownerUid = createUid( ownerPath );

                StorageItem ownerItem = super.doRetrieveItem( localOnly, ownerUid, context );

                if ( StorageFileItem.class.isAssignableFrom( ownerItem.getClass() ) )
                {
                    StorageFileItem owner = (StorageFileItem) ownerItem;

                    String hash = null;

                    if ( uid.getPath().endsWith( ".sha1" ) )
                    {
                        hash = owner.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );
                    }
                    else
                    {
                        hash = owner.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY );
                    }

                    StringContentLocator content = new StringContentLocator( hash );

                    DefaultStorageFileItem result = new DefaultStorageFileItem(
                        this,
                        uid.getPath(),
                        owner.isReadable(),
                        owner.isWritable(),
                        content );

                    result.overlay( owner );

                    result.getItemContext().putAll( owner.getItemContext() );

                    result.setMimeType( "text/plain" );

                    result.setLength( hash.length() );

                    return result;
                }
                else
                {
                    // this is not a file?
                    return super.doRetrieveItem( localOnly, uid, context );
                }
            }
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "The serving of item " + uid.toString() + " is forbidden by Maven repository policy." );
            }
            throw new ItemNotFoundException( uid );
        }

    }

    protected AbstractStorageItem doRetrieveRemoteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            StorageException
    {
        if ( uid.getPath().endsWith( ".sha1" ) || uid.getPath().endsWith( ".md5" ) )
        {
            // checksums go the simple way
            return super.doRetrieveRemoteItem( uid, context );
        }
        else
        {
            // artifacts and poms have "special treat", they should come with checksums
            return doRetrieveRemoteMavenItem( 0, uid, context, super.doRetrieveRemoteItem( uid, context ) );
        }
    }

    protected AbstractStorageItem doRetrieveRemoteMavenItem( int tried, RepositoryItemUid uid,
        Map<String, Object> context, AbstractStorageItem result )
        throws ItemNotFoundException,
            StorageException
    {
        if ( tried == 1 )
        {
            if ( ChecksumPolicy.STRICT.equals( getChecksumPolicy() )
                || ChecksumPolicy.STRICT_IF_EXISTS.equals( getChecksumPolicy() ) )
            {
                try
                {
                    try
                    {
                        getLocalStorage().deleteItem( uid );
                    }
                    catch ( ItemNotFoundException e )
                    {
                        // neglect
                    }
                    try
                    {
                        getLocalStorage().deleteItem( uid.getRepository().createUid( uid.getPath() + ".sha1" ) );
                    }
                    catch ( ItemNotFoundException e )
                    {
                        // neglect
                    }
                    try
                    {
                        getLocalStorage().deleteItem( uid.getRepository().createUid( uid.getPath() + ".md5" ) );
                    }
                    catch ( ItemNotFoundException e )
                    {
                        // neglect
                    }

                }
                catch ( UnsupportedStorageOperationException e )
                {
                    // huh?
                }

                NexusArtifactEvent nae = new NexusArtifactEvent();

                nae.setAction( NexusArtifactEvent.ACTION_BROKEN_WRONG_REMOTE_CHECKSUM );

                nae.setEventDate( new Date() );

                nae.setMessage( "The artifact " + result.getPath()
                    + " and it's remote checksums does not match in repository " + result.getRepositoryId()
                    + "! The checksumPolicy of repository forbids downloading of it." );

                nae.setEventContext( result.getItemContext() );

                NexusItemInfo ai = new NexusItemInfo();

                ai.setPath( result.getPath() );

                ai.setRepositoryId( result.getRepositoryId() );

                ai.setRemoteUrl( result.getRemoteUrl() );

                nae.setNexusItemInfo( ai );

                feedRecorder.addNexusArtifactEvent( nae );

                throw new ItemNotFoundException( uid );
            }
            else if ( ChecksumPolicy.WARN.equals( getChecksumPolicy() ) )
            {
                getLogger().warn(
                    "The artifact " + uid.toString() + " and it's remote checksums does not match in repository "
                        + result.getRepositoryId() + "!" );

                NexusArtifactEvent nae = new NexusArtifactEvent();

                nae.setAction( NexusArtifactEvent.ACTION_BROKEN_WRONG_REMOTE_CHECKSUM );

                nae.setEventDate( new Date() );

                nae.setMessage( "Warning, the artifact " + result.getPath()
                    + " and it's remote checksums does not match in repository " + result.getRepositoryId() + "!" );

                nae.setEventContext( result.getItemContext() );

                NexusItemInfo ai = new NexusItemInfo();

                ai.setPath( result.getPath() );

                ai.setRepositoryId( result.getRepositoryId() );

                ai.setRemoteUrl( result.getRemoteUrl() );

                nae.setNexusItemInfo( ai );

                feedRecorder.addNexusArtifactEvent( nae );

                return result;
            }
            else
            {
                return result;
            }
        }

        // this is not a checksum for sure (see doRetrieveItem in this class), hence go remote as should
        // but use remote maven repo checksum to verify transport success

        if ( getChecksumPolicy().shouldCheckChecksum() && StorageFileItem.class.isAssignableFrom( result.getClass() ) )
        {
            String hashKey = null;

            RepositoryItemUid hashUid = null;

            DefaultStorageFileItem hashItem = null;

            // we prefer sha1
            try
            {
                hashKey = DigestCalculatingInspector.DIGEST_SHA1_KEY;

                hashUid = uid.getRepository().createUid( uid.getPath() + ".sha1" );

                hashItem = (DefaultStorageFileItem) doRetrieveRemoteItem( hashUid, context );

                getLocalStorage().touchItemRemoteChecked( hashUid );
            }
            catch ( ItemNotFoundException esha1 )
            {
                try
                {
                    hashKey = DigestCalculatingInspector.DIGEST_MD5_KEY;

                    hashUid = uid.getRepository().createUid( uid.getPath() + ".md5" );

                    hashItem = (DefaultStorageFileItem) doRetrieveRemoteItem( hashUid, context );

                    getLocalStorage().touchItemRemoteChecked( hashUid );
                }
                catch ( ItemNotFoundException emd5 )
                {
                    // it seems we don't have any remotely available checksum
                    getLogger().debug( "Item checksums (SHA1, MD5) remotely unavailable " + uid.toString() );

                    if ( ChecksumPolicy.STRICT.equals( getChecksumPolicy() ) )
                    {
                        NexusArtifactEvent nae = new NexusArtifactEvent();

                        nae.setAction( NexusArtifactEvent.ACTION_BROKEN_WRONG_REMOTE_CHECKSUM );

                        nae.setEventDate( new Date() );

                        nae.setEventContext( result.getItemContext() );

                        nae.setMessage( "The artifact " + result.getPath() + " has no remote checksum in repository "
                            + result.getRepositoryId()
                            + "! The checksumPolicy of repository forbids downloading of it." );

                        NexusItemInfo ai = new NexusItemInfo();

                        ai.setPath( result.getPath() );

                        ai.setRepositoryId( result.getRepositoryId() );

                        ai.setRemoteUrl( result.getRemoteUrl() );

                        nae.setNexusItemInfo( ai );

                        feedRecorder.addNexusArtifactEvent( nae );

                        throw new ItemNotFoundException( uid );
                    }
                    else
                    {
                        NexusArtifactEvent nae = new NexusArtifactEvent();

                        nae.setAction( NexusArtifactEvent.ACTION_BROKEN_WRONG_REMOTE_CHECKSUM );

                        nae.setEventDate( new Date() );

                        nae.setEventContext( result.getItemContext() );

                        nae.setMessage( "Warning, the artifact " + result.getPath()
                            + " has no remote checksum in repository " + result.getRepositoryId() + "!" );

                        NexusItemInfo ai = new NexusItemInfo();

                        ai.setPath( result.getPath() );

                        ai.setRepositoryId( result.getRepositoryId() );

                        ai.setRemoteUrl( result.getRemoteUrl() );

                        nae.setNexusItemInfo( ai );

                        feedRecorder.addNexusArtifactEvent( nae );

                        return result;
                    }
                }
            }

            String remoteHash = null;

            InputStream hashItemContent = null;

            try
            {
                hashItemContent = hashItem.getInputStream();

                remoteHash = StringUtils.chomp( IOUtil.toString( hashItemContent ) ).trim().split( " " )[0];
            }
            catch ( IOException e )
            {
                // it seems we don't have any remotely available checksum
                getLogger().warn( "Cannot read hash string for remotely fetched StorageFileItem: " + uid.toString(), e );

                if ( ChecksumPolicy.STRICT.equals( getChecksumPolicy() ) )
                {
                    throw new ItemNotFoundException( uid );
                }
                else
                {
                    return result;
                }
            }
            finally
            {
                IOUtil.close( hashItemContent );
            }

            if ( remoteHash != null && remoteHash.equals( result.getAttributes().get( hashKey ) ) )
            {
                // the transfer succeeded and we have it ok already stored
                return result;
            }
            else
            {
                // the hashes differ, lets redownload it and try it again
                return doRetrieveRemoteMavenItem( ++tried, uid, context, super.doRetrieveRemoteItem( uid, context ) );
            }
        }
        else
        {
            return result;
        }
    }

    protected void markItemRemotelyChecked( RepositoryItemUid uid )
        throws StorageException,
            ItemNotFoundException
    {
        super.markItemRemotelyChecked( uid );

        RepositoryItemUid sha1Uid = uid.getRepository().createUid( uid.getPath() + ".sha1" );

        if ( getLocalStorage().containsItem( sha1Uid ) )
        {
            super.markItemRemotelyChecked( sha1Uid );
        }

        RepositoryItemUid md5Uid = uid.getRepository().createUid( uid.getPath() + ".md5" );

        if ( getLocalStorage().containsItem( md5Uid ) )
        {
            super.markItemRemotelyChecked( md5Uid );
        }
    }

    public InputStream retrieveItemContent( RepositoryItemUid uid )
        throws IllegalArgumentException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        if ( shouldServeByPolicies( uid ) )
        {
            return super.retrieveItemContent( uid );
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "The serving of item " + uid.toString() + " is forbidden by Maven repository policy." );
            }
            throw new ItemNotFoundException( uid );
        }
    }

    public void storeItem( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            StorageException
    {
        if ( shouldServeByPolicies( item.getRepositoryItemUid() ) )
        {
            super.storeItem( item );
        }
        else
        {
            throw new UnsupportedStorageOperationException( "Storing of item " + item.getRepositoryItemUid().toString()
                + " is forbidden by Repository policy." );
        }
    }

}
