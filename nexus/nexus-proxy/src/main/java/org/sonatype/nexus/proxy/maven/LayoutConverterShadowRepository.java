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
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * Base class for shadows that make "gateways" from M1 to M2 lauouts and vice versa.
 * 
 * @author cstamas
 */
public abstract class LayoutConverterShadowRepository
    extends ShadowRepository
    implements MavenRepository
{
    /**
     * The GAV Calculator.
     * 
     * @plexus.requirement role-hint="maven1"
     */
    private GavCalculator m1GavCalculator;

    /**
     * The GAV Calculator.
     * 
     * @plexus.requirement role-hint="maven2"
     */
    private GavCalculator m2GavCalculator;

    /**
     * Metadata manager.
     * 
     * @plexus.requirement
     */
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
    {
        // we allow only MavenRepository instances as masters
        if ( !MavenRepository.class.isAssignableFrom( masterRepository.getClass() ) )
        {
            throw new IllegalArgumentException(
                "This shadow repository needs master repository which implements MavenRepository interface!" );
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
        return ( (MavenRepository) getMasterRepository() ).getRepositoryPolicy();
    }

    public MetadataManager getMetadataManager()
    {
        return metadataManager;
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

    public void deleteItemWithChecksums( RepositoryItemUid uid, Map<String, Object> ctx )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "deleteItemWithChecksums() :: " + uid.toString() );
        }

        deleteItem( uid, ctx );

        RepositoryItemUid sha1Uid = createUidForPath( uid.getPath() + ".sha1" );

        try
        {
            deleteItem( sha1Uid, ctx );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore not found
        }

        RepositoryItemUid md5Uid = createUidForPath( uid.getPath() + ".md5" );

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
        throws ItemNotFoundException
    {
        Gav gav = getM1GavCalculator().pathToGav( path );

        // Unsupported path
        if ( gav == null )
        {
            throw new ItemNotFoundException( path );
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
        throws ItemNotFoundException
    {
        Gav gav = getM2GavCalculator().pathToGav( path );

        // Unsupported path
        if ( gav == null )
        {
            throw new ItemNotFoundException( path );
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
