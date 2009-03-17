/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
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
     * The artifact packaging mapper.
     */
    @Requirement
    private ArtifactPackagingMapper artifactPackagingMapper;

    /**
     * ArtifactStoreHelper.
     */
    private ArtifactStoreHelper artifactStoreHelper;

    @Override
    public MavenRepository getMasterRepository()
    {
        return super.getMasterRepository().adaptToFacet( MavenRepository.class );
    }

    @Override
    public void setMasterRepository( Repository masterRepository )
        throws IncompatibleMasterRepositoryException
    {
        // we allow only MavenRepository instances as masters
        if ( !masterRepository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
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

    public ArtifactPackagingMapper getArtifactPackagingMapper()
    {
        return artifactPackagingMapper;
    }

    public RepositoryPolicy getRepositoryPolicy()
    {
        return getMasterRepository().getRepositoryPolicy();
    }

    public void setRepositoryPolicy( RepositoryPolicy repositoryPolicy )
    {
        throw new UnsupportedOperationException( "This method is not supported on Repository of type SHADOW" );
    }

    public MetadataManager getMetadataManager()
    {
        return metadataManager;
    }

    public boolean recreateMavenMetadata( ResourceStoreRequest request )
    {
        return false;
    }

    public void storeItemWithChecksums( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        String originalPath = request.getRequestPath();

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

            StorageFileItem storedFile = (StorageFileItem) retrieveItem( false, request );

            String sha1Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );

            String md5Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY );

            if ( !StringUtils.isEmpty( sha1Hash ) )
            {
                request.setRequestPath( storedFile.getPath() + ".sha1" );

                storeItem( false, new DefaultStorageFileItem( this, request, true, true, new StringContentLocator(
                    sha1Hash ) ) );
            }

            if ( !StringUtils.isEmpty( md5Hash ) )
            {
                request.setRequestPath( storedFile.getPath() + ".md5" );

                storeItem( false, new DefaultStorageFileItem( this, request, true, true, new StringContentLocator(
                    md5Hash ) ) );
            }
        }
        catch ( ItemNotFoundException e )
        {
            throw new StorageException( "Storage inconsistency!", e );
        }
        finally
        {
            request.setRequestPath( originalPath );
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

    public void storeItemWithChecksums( boolean fromTask, AbstractStorageItem item )
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
                storeItem( fromTask, item );
            }
            catch ( IOException e )
            {
                throw new StorageException( "Could not get the content from the ContentLocator!", e );
            }

            StorageFileItem storedFile = (StorageFileItem) retrieveItem( fromTask, new ResourceStoreRequest( item ) );

            ResourceStoreRequest req = new ResourceStoreRequest( storedFile );

            String sha1Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );

            String md5Hash = storedFile.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY );

            if ( !StringUtils.isEmpty( sha1Hash ) )
            {
                req.setRequestPath( item.getPath() + ".sha1" );

                storeItem( fromTask, new DefaultStorageFileItem( this, req, true, true, new StringContentLocator(
                    sha1Hash ) ) );
            }

            if ( !StringUtils.isEmpty( md5Hash ) )
            {
                req.setRequestPath( item.getPath() + ".md5" );

                storeItem( fromTask, new DefaultStorageFileItem( this, req, true, true, new StringContentLocator(
                    md5Hash ) ) );
            }
        }
        catch ( ItemNotFoundException e )
        {
            throw new StorageException( "Storage inconsistency!", e );
        }
    }

    public void deleteItemWithChecksums( boolean fromTask, ResourceStoreRequest request )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "deleteItemWithChecksums() :: " + request.toString() );
        }

        deleteItem( fromTask, request );

        try
        {
            request.pushRequestPath( request.getRequestPath() + ".sha1" );

            deleteItem( fromTask, request );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }
        finally
        {
            request.popRequestPath();
        }

        try
        {
            request.pushRequestPath( request.getRequestPath() + ".md5" );

            deleteItem( fromTask, request );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }
        finally
        {
            request.popRequestPath();
        }
    }

    public ArtifactStoreHelper getArtifactStoreHelper()
    {
        if ( artifactStoreHelper == null )
        {
            artifactStoreHelper = new ArtifactStoreHelper( this );
        }

        return artifactStoreHelper;
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

    @Override
    protected void deleteLink( StorageItem item )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        String shadowPath = transformMaster2Shadow( item.getPath() );

        if ( shadowPath != null )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( item );

            deleteItem( false, request );
        }
    }

    @Override
    protected void createLink( StorageItem item )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException
    {
        String shadowPath = transformMaster2Shadow( item.getPath() );

        if ( shadowPath != null )
        {
            ResourceStoreRequest req = new ResourceStoreRequest( shadowPath );

            req.getRequestContext().putAll( item.getItemContext() );

            DefaultStorageLinkItem link = new DefaultStorageLinkItem( this, req, true, true, item
                .getRepositoryItemUid() );

            storeItem( false, link );
        }
    }

    /**
     * Gets the shadow path from master path. If path is not transformable, return null.
     * 
     * @param path the path
     * @return the shadow path
     */
    protected abstract String transformMaster2Shadow( String path );

    @Override
    protected StorageItem doRetrieveItem( ResourceStoreRequest request )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        StorageItem result = null;

        try
        {
            result = super.doRetrieveItem( request );

            return result;
        }
        catch ( ItemNotFoundException e )
        {
            // if it is thrown by super.doRetrieveItem()
            String transformedPath = transformShadow2Master( request.getRequestPath() );

            if ( transformedPath == null )
            {
                throw new ItemNotFoundException( request, this );
            }

            // delegate the call to the master
            request.pushRequestPath( transformedPath );

            try
            {
                result = doRetrieveItemFromMaster( request );
            }
            finally
            {
                request.popRequestPath();
            }

            return result;
        }
    }

    /**
     * Gets the master path from shadow path. If path is not transformable, return null.
     * 
     * @param path the path
     * @return the master path
     */
    protected abstract String transformShadow2Master( String path );

}
