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
package org.sonatype.nexus.proxy.maven.maven2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.M2ArtifactRecognizer;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.ArtifactPackagingMapper;
import org.sonatype.nexus.proxy.maven.ArtifactStoreHelper;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.MetadataManager;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.DefaultShadowRepository;
import org.sonatype.nexus.proxy.repository.IncompatibleMasterRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.util.AlphanumComparator;

/**
 * A M2 shadow repository that makes constraints against artifact versions, simply blocking maven plugin discovery using
 * metadata. This shadow will (if set) report specific plugin versions only.
 * 
 * @author cstamas
 */
@Component( role = ShadowRepository.class, hint = "m2-constrained", instantiationStrategy = "per-lookup" )
public class ConstrainedM2ShadowRepository
    extends DefaultShadowRepository
    implements MavenRepository
{
    /**
     * The GAV Calculator.
     */
    @Requirement( hint = "maven2" )
    private GavCalculator gavCalculator;

    /**
     * The ContentClass.
     */
    @Requirement( hint = "maven2" )
    private ContentClass contentClass;

    /**
     * The artifact packaging mapper.
     */
    @Requirement
    private ArtifactPackagingMapper artifactPackagingMapper;

    /**
     * Metadata manager.
     */
    @Requirement
    private MetadataManager metadataManager;

    /**
     * ArtifactStoreHelper.
     */
    private ArtifactStoreHelper artifactStoreHelper;

    private Map<String, String> versionMap;

    public Map<String, String> getVersionMap()
    {
        return versionMap;
    }

    public void setVersionMap( Map<String, String> versionMap )
    {
        this.versionMap = versionMap;
    }

    public MavenRepository getMasterRepository()
    {
        return (MavenRepository) super.getMasterRepository();
    }

    public void setMasterRepository( Repository masterRepository )
        throws IncompatibleMasterRepositoryException
    {
        // we allow only MavenRepository instances as masters
        if ( !MavenRepository.class.isAssignableFrom( masterRepository.getClass() ) )
        {
            throw new IncompatibleMasterRepositoryException(
                "This shadow repository needs master repository which implements MavenRepository interface!",
                this,
                masterRepository );
        }

        super.setMasterRepository( masterRepository );
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public ContentClass getMasterRepositoryContentClass()
    {
        return contentClass;
    }

    public RepositoryPolicy getRepositoryPolicy()
    {
        return getMasterRepository().getRepositoryPolicy();
    }

    public ArtifactPackagingMapper getArtifactPackagingMapper()
    {
        return artifactPackagingMapper;
    }

    protected ArtifactStoreHelper getArtifactStoreHelper()
    {
        if ( artifactStoreHelper == null )
        {
            artifactStoreHelper = new ArtifactStoreHelper( this );
        }
        return artifactStoreHelper;
    }

    public boolean recreateMavenMetadata( String path )
    {
        return false;
    }

    @Override
    protected String transformMaster2Shadow( String path )
    {
        return path;
    }

    @Override
    protected String transformShadow2Master( String path )
    {
        return path;
    }

    public GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

    public MetadataManager getMetadataManager()
    {
        return metadataManager;
    }

    public void storeItemWithChecksums( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
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

            StorageFileItem storedFile = (StorageFileItem) retrieveItem( true, itemUid, null );

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
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
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
                    item.getPath() + ".sha1",
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

        deleteItem( uid, context );

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
    }

    // =================================================================================
    // ShadowRepository customizations

    protected StorageItem doRetrieveItem( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        if ( M2ArtifactRecognizer.isMetadata( uid.getPath() ) && !M2ArtifactRecognizer.isChecksum( uid.getPath() ) )
        {
            // this is metadata file
            StorageItem item = super.doRetrieveItem( localOnly, uid, context );

            if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
            {
                StorageFileItem file = (StorageFileItem) item;

                // remote item is not reusable, and we usually cache remote stuff locally
                ByteArrayInputStream backup = null;
                try
                {
                    // prepare for the worst, backup it to be able to reemit it
                    ByteArrayOutputStream backup1 = new ByteArrayOutputStream();

                    IOUtil.copy( file.getInputStream(), backup1 );

                    backup = new ByteArrayInputStream( backup1.toByteArray() );

                    // Metadata is small, let's do it in memory
                    MetadataXpp3Reader metadataReader = new MetadataXpp3Reader();

                    InputStreamReader isr = new InputStreamReader( backup );

                    Metadata imd = metadataReader.read( isr );

                    // sanity check, the metadata should have gid and aid as we have from path
                    GA ga = getGaForMetadata( uid.getPath() );

                    if ( shouldProcess( ga ) && ga.getGroupId().equals( imd.getGroupId() )
                        && ga.getArtifactId().equals( imd.getArtifactId() ) && imd.getVersioning() != null )
                    {
                        VersionRange versionRange = VersionRange.createFromVersionSpec( getVersionForGA( ga ) );

                        String versionStr;

                        DefaultArtifactVersion version;

                        List<String> versions = imd.getVersioning().getVersions();

                        for ( Iterator<String> i = versions.iterator(); i.hasNext(); )
                        {
                            versionStr = i.next();

                            version = new DefaultArtifactVersion( versionStr );

                            if ( !versionRange.containsVersion( version ) )
                            {
                                i.remove();
                            }
                        }
                        if ( versions.size() > 0 )
                        {
                            Collections.sort( versions, new AlphanumComparator() );

                            imd.setVersion( versions.get( versions.size() - 1 ) );

                            imd.getVersioning().setLatest( imd.getVersion() );

                            imd.getVersioning().setRelease( imd.getVersion() );
                        }
                        else
                        {
                            imd.setVersion( null );

                            imd.getVersioning().setLatest( null );

                            imd.getVersioning().setRelease( null );
                        }

                        // serialize and swap the new metadata
                        MetadataXpp3Writer metadataWriter = new MetadataXpp3Writer();

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();

                        OutputStreamWriter osw = new OutputStreamWriter( bos );

                        metadataWriter.write( osw, imd );

                        ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );

                        file.setContentLocator( new PreparedContentLocator( bis ) );
                    }
                    else
                    {
                        // get backup and continue operation
                        backup.reset();

                        file.setContentLocator( new PreparedContentLocator( backup ) );
                    }

                    return file;
                }
                catch ( Exception e )
                {
                    getLogger().error( "Exception during repository metadata constraining.", e );

                    if ( backup != null )
                    {
                        // get backup and continue operation
                        backup.reset();

                        file.setContentLocator( new PreparedContentLocator( backup ) );
                    }

                    return file;
                }

            }
            else
            {
                // what the hell is this???
                return item;
            }
        }
        else
        {
            // the "normal" way, serving the file from repo (cache or remote, whatever)
            return super.doRetrieveItem( localOnly, uid, context );
        }
    }

    protected GA getGaForMetadata( String path )
    {
        String s = path.startsWith( "/" ) ? path.substring( 1 ) : path;

        int aEndPos = s.lastIndexOf( '/' );

        if ( aEndPos == -1 )
        {
            return null;
        }

        int gEndPos = s.lastIndexOf( '/', aEndPos - 1 );

        if ( gEndPos == -1 )
        {
            return null;
        }

        String g = s.substring( 0, gEndPos ).replace( '/', '.' );
        String a = s.substring( gEndPos + 1, aEndPos );

        return new GA( g, a );
    }

    protected boolean shouldProcess( GA ga )
    {
        if ( ga == null )
        {
            return false;
        }

        return getVersionForGA( ga ) != null;
    }

    protected String getVersionForGA( GA ga )
    {
        return getVersionMap().get( ga.getGroupId() + ":" + ga.getArtifactId() );
    }

    private class GA
    {
        private String groupId;

        private String artifactId;

        public GA( String gid, String aid )
        {
            this.groupId = gid;
            this.artifactId = aid;
        }

        public String getGroupId()
        {
            return groupId;
        }

        public String getArtifactId()
        {
            return artifactId;
        }
    }

    // =======================================================================================
    // MavenRepository iface, delegates to master simply

    public ChecksumPolicy getChecksumPolicy()
    {
        return getMasterRepository().getChecksumPolicy();
    }

    public int getMetadataMaxAge()
    {
        return getMasterRepository().getMetadataMaxAge();
    }

    public int getReleaseMaxAge()
    {
        return getMasterRepository().getReleaseMaxAge();
    }

    public int getSnapshotMaxAge()
    {
        return getMasterRepository().getSnapshotMaxAge();
    }

    public boolean isCleanseRepositoryMetadata()
    {
        return getMasterRepository().isCleanseRepositoryMetadata();
    }

    public boolean isFixRepositoryChecksums()
    {
        return getMasterRepository().isFixRepositoryChecksums();
    }

    public void setChecksumPolicy( ChecksumPolicy checksumPolicy )
    {
        throw new UnsupportedOperationException( "This method is not supported on Repository of type SHADOW" );
    }

    public void setCleanseRepositoryMetadata( boolean cleanseRepositoryMetadata )
    {
        throw new UnsupportedOperationException( "This method is not supported on Repository of type SHADOW" );
    }

    public void setFixRepositoryChecksums( boolean fixRepositoryChecksums )
    {
        throw new UnsupportedOperationException( "This method is not supported on Repository of type SHADOW" );
    }

    public void setMetadataMaxAge( int metadataMaxAge )
    {
        throw new UnsupportedOperationException( "This method is not supported on Repository of type SHADOW" );
    }

    public void setReleaseMaxAge( int releaseMaxAge )
    {
        throw new UnsupportedOperationException( "This method is not supported on Repository of type SHADOW" );
    }

    public void setRepositoryPolicy( RepositoryPolicy repositoryPolicy )
    {
        throw new UnsupportedOperationException( "This method is not supported on Repository of type SHADOW" );
    }

    public void setSnapshotMaxAge( int snapshotMaxAge )
    {
        throw new UnsupportedOperationException( "This method is not supported on Repository of type SHADOW" );
    }

}
