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

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.M1GavCalculator;
import org.sonatype.nexus.artifact.M2GavCalculator;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ArtifactStore;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

/**
 * Base class for shadows that make "gateways" from M1 to M2 lauouts and vice versa.
 * 
 * @author cstamas
 */
public abstract class LayoutConverterShadowRepository
    extends ShadowRepository
    implements ArtifactStore
{

    /**
     * Implements ArtifactStore
     */
    public StorageFileItem retrieveArtifactPom( String groupId, String artifactId, String version )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        Gav gav = new Gav( groupId, artifactId, version, null, "pom", null, null, null, false, false, false );

        RepositoryItemUid uid = new RepositoryItemUid( this, gav2path( gav ) );

        StorageItem item = retrieveItem( true, uid );

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            return (StorageFileItem) item;
        }
        else
        {
            throw new StorageException( "The POM retrieval returned non-file, path:" + uid.getPath() );
        }
    }

    /**
     * Converts GAV to path according to layout.
     * 
     * @param gav
     * @return
     */
    protected abstract String gav2path( Gav gav );

    /**
     * Transforms a full artifact path from M1 layout to M2 layout.
     * 
     * @param path
     * @return
     */
    protected String transformM1toM2( String path )
    throws ItemNotFoundException
    {
        Gav gav = M1GavCalculator.calculate( path );
        //Unsupported path
        if (gav == null)
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
        Gav gav = M2GavCalculator.calculate( path );
        
        //Unsupported path
        if (gav == null)
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
