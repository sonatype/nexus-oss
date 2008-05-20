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
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.M1ArtifactRecognizer;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * The default M1Repository. This class adds snapshot/release sensing and differentiated expiration handling and repo
 * policies for them.
 * 
 * @author cstamas
 * @plexus.component instantiation-strategy="per-lookup" role="org.sonatype.nexus.proxy.repository.Repository" role-hint="maven1"
 */
public class M1Repository
    extends AbstractMavenRepository
{
    /**
     * The GAV Calculator.
     * 
     * @plexus.requirement role-hint="m1"
     */
    private GavCalculator gavCalculator;

    private ContentClass contentClass = new Maven1ContentClass();

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

    /**
     * Should serve by policies.
     * 
     * @param uid the uid
     * @return true, if successful
     */
    public boolean shouldServeByPolicies( RepositoryItemUid uid )
    {
        if ( M1ArtifactRecognizer.isMetadata( uid.getPath() ) )
        {
            if ( M1ArtifactRecognizer.isSnapshot( uid.getPath() ) )
            {
                return RepositoryPolicy.SNAPSHOT.equals( getRepositoryPolicy() );
            }
            else
            {
                // metadatas goes always
                return true;
            }
        }
        // we are using Gav to test the path
        Gav gav = gavCalculator.pathToGav( uid.getPath() );
        if ( gav == null )
        {
            return true;
        }
        else
        {
            if ( gav.isSnapshot() )
            {
                // snapshots goes if enabled
                return RepositoryPolicy.SNAPSHOT.equals( getRepositoryPolicy() );
            }
            else
            {
                return RepositoryPolicy.RELEASE.equals( getRepositoryPolicy() );
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
        Gav gav = gavCalculator.pathToGav( item.getPath() );

        if ( gav == null )
        {
            // this is not an artifact, it is just any "file"
            return super.isOld( item );
        }
        // it is a release
        return isOld( getReleaseMaxAge(), item );
    }
}
