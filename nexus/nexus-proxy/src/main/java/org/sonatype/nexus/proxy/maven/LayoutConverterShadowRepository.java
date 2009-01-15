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
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepository;
import org.sonatype.nexus.proxy.repository.IncompatibleMasterRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * Base class for shadows that make "gateways" from M1 to M2 lauouts and vice versa.
 * 
 * @author cstamas
 */
public abstract class LayoutConverterShadowRepository
    extends AbstractShadowRepository
    implements MavenRepository
{
    /**
     * The GAV Calculator.
     */
    @Requirement( hint = "maven1" )
    private GavCalculator m1GavCalculator;

    /**
     * The GAV Calculator.
     */
    @Requirement( hint = "maven2" )
    private GavCalculator m2GavCalculator;

    /**
     * Metadata manager.
     */
    @Requirement
    private MetadataManager metadataManager;

    /**
     * ArtifactStoreHelper.
     */
    private ArtifactStoreHelper artifactStoreHelper;

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

    public GavCalculator getM1GavCalculator()
    {
        return m1GavCalculator;
    }

    public GavCalculator getM2GavCalculator()
    {
        return m2GavCalculator;
    }

    public RepositoryPolicy getRepositoryPolicy()
    {
        return getMasterRepository().getRepositoryPolicy();
    }

    public MetadataManager getMetadataManager()
    {
        return metadataManager;
    }

    public boolean recreateMavenMetadata( String path )
    {
        return false;
    }

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

    public void deleteItemWithChecksums( RepositoryItemUid uid, Map<String, Object> ctx )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "deleteItemWithChecksums() :: " + uid.toString() );
        }

        deleteItem( uid, ctx );

        RepositoryItemUid sha1Uid = createUid( uid.getPath() + ".sha1" );

        try
        {
            deleteItem( sha1Uid, ctx );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }

        RepositoryItemUid md5Uid = createUid( uid.getPath() + ".md5" );

        try
        {
            deleteItem( md5Uid, ctx );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }
    }

    protected ArtifactStoreHelper getArtifactStoreHelper()
    {
        if ( artifactStoreHelper == null )
        {
            artifactStoreHelper = new ArtifactStoreHelper( this );
        }
        return artifactStoreHelper;
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

    // =================================================================================
    // ShadowRepository customizations

    /**
     * Transforms a full artifact path from M1 layout to M2 layout.
     * 
     * @param path
     * @return
     */
    protected String transformM1toM2( String path )
    {
        Gav gav = getM1GavCalculator().pathToGav( path );

        // Unsupported path
        if ( gav == null )
        {
            return null;
        }
        // m2 repo is layouted as:
        // g/i/d
        // aid
        // version
        // files

        StringBuffer sb = new StringBuffer( RepositoryItemUid.PATH_ROOT );
        sb.append( gav.getGroupId().replaceAll( "\\.", "/" ) );
        sb.append( RepositoryItemUid.PATH_SEPARATOR );
        sb.append( gav.getArtifactId() );
        sb.append( RepositoryItemUid.PATH_SEPARATOR );
        sb.append( gav.getVersion() );
        sb.append( RepositoryItemUid.PATH_SEPARATOR );
        sb.append( gav.getName() );
        return sb.toString();
    }

    /**
     * Transforms a full artifact path from M2 layout to M1 layout.
     * 
     * @param path
     * @return
     */
    protected String transformM2toM1( String path )
    {
        Gav gav = getM2GavCalculator().pathToGav( path );

        // Unsupported path
        if ( gav == null )
        {
            return null;
        }
        // m1 repo is layouted as:
        // g.i.d
        // poms/jars/java-sources/licenses
        // files
        StringBuffer sb = new StringBuffer( RepositoryItemUid.PATH_ROOT );
        sb.append( gav.getGroupId() );
        sb.append( RepositoryItemUid.PATH_SEPARATOR );
        sb.append( gav.getExtension() + "s" );
        sb.append( RepositoryItemUid.PATH_SEPARATOR );
        sb.append( gav.getName() );
        return sb.toString();
    }

}
