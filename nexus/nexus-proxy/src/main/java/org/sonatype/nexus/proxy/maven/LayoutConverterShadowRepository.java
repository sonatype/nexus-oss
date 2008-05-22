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

import java.io.InputStream;
import java.util.Collection;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
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
     * ArtifactStoreHelper.
     */
    private ArtifactStoreHelper artifactStoreHelper;

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

    public void storeItemWithChecksums( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            StorageException
    {
        getArtifactStoreHelper().storeItemWithChecksums( item );
    }

    public StorageFileItem retrieveArtifactPom( GAVRequest gavRequest )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        return getArtifactStoreHelper().retrieveArtifactPom( gavRequest );
    }

    public StorageFileItem retrieveArtifact( GAVRequest gavRequest )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        return getArtifactStoreHelper().retrieveArtifact( gavRequest );
    }

    public void storeArtifact( GAVRequest gavRequest, InputStream is )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        getArtifactStoreHelper().storeArtifact( gavRequest, is );
    }

    public void storeArtifactPom( GAVRequest gavRequest, InputStream is )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        getArtifactStoreHelper().storeArtifactPom( gavRequest, is );
    }

    public void storeArtifactWithGeneratedPom( GAVRequest gavRequest, InputStream is )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        getArtifactStoreHelper().storeArtifactWithGeneratedPom( gavRequest, is );
    }

    public void deleteArtifact( GAVRequest gavRequest, boolean withAllSubordinates )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        getArtifactStoreHelper().deleteArtifact( gavRequest, withAllSubordinates );
    }

    public Collection<Gav> listArtifacts( GAVRequest gavRequest )
    {
        return getArtifactStoreHelper().listArtifacts( gavRequest );
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
