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
import org.sonatype.nexus.artifact.M1ArtifactRecognizer;
import org.sonatype.nexus.artifact.M1GavCalculator;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * The default M1Repository. This class adds snapshot/release sensing and differentiated expiration handling and repo
 * policies for them.
 * 
 * @author cstamas
 * @plexus.component instantiation-strategy="per-lookup" role-hint="maven1"
 */
public class M1Repository
    extends AbstractMavenRepository
{
    private ContentClass contentClass = new Maven1ContentClass();

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public StorageFileItem retrieveArtifactPom( String groupId, String artifactId, String version )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        Gav gav = new Gav( groupId, artifactId, version, null, "pom", null, null, null, false, false, false );

        RepositoryItemUid uid = new RepositoryItemUid( this, M1GavCalculator.calculateRepositoryPath( gav ) );

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
     * Should serve by policies.
     * 
     * @param uid the uid
     * @return true, if successful
     */
    protected boolean shouldServeByPolicies( RepositoryItemUid uid )
    {
        if ( M1ArtifactRecognizer.isMetadata( uid.getPath() ) )
        {
            if ( M1ArtifactRecognizer.isSnapshot( uid.getPath() ) )
            {
                return isShouldServeSnapshots();
            }
            else
            {
                // metadatas goes always
                return true;
            }
        }
        // we are using Gav to test the path
        Gav gav = M1GavCalculator.calculate( uid.getPath() );
        if ( gav == null )
        {
            return true;
        }
        else
        {
            if ( gav.isSnapshot() )
            {
                // snapshots goes if enabled
                return isShouldServeSnapshots();
            }
            else
            {
                return isShouldServeReleases();
            }
        }
    }

    protected boolean isOld( StorageItem item )
    {
        if ( M1ArtifactRecognizer.isMetadata( item.getPath() ) )
        {
            return isOld( getMetadataMaxAge(), item );
        }
        if ( M1ArtifactRecognizer.isSnapshot( item.getPath() ) )
        {
            return isOld( getSnapshotMaxAge(), item );
        }

        // we are using Gav to test the path
        Gav gav = M1GavCalculator.calculate( item.getPath() );

        if ( gav == null )
        {
            // this is not an artifact, it is just any "file"
            return super.isOld( item );
        }
        // it is a release
        return isOld( getReleaseMaxAge(), item );
    }
}
