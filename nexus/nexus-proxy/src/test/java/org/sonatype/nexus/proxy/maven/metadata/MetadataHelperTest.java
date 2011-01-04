/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.M2GavCalculator;

/**
 * @author juven
 */
public class MetadataHelperTest
    extends TestCase
{

    public void testVersioningArtifactDirectory()
        throws Exception
    {
        List<String> orderedVersions = new ArrayList<String>();
        orderedVersions.add( "1.0.0-alpha-5" );
        orderedVersions.add( "1.0.0-beta-3" );
        orderedVersions.add( "1.0.0-beta-4" );
        orderedVersions.add( "1.0.0-beta-6-SNAPSHOT" );
        orderedVersions.add( "1.0.0" );
        orderedVersions.add( "1.0.1" );
        orderedVersions.add( "1.0.3-SNAPSHOT" );
        orderedVersions.add( "1.1-M1" );
        orderedVersions.add( "1.2.0-beta-1" );
        orderedVersions.add( "1.2.0-SNAPSHOT" );
        orderedVersions.add( "1.2.0" );
        orderedVersions.add( "1.2.0.5-SNAPSHOT" );
        orderedVersions.add( "1.3.0-SNAPSHOT" );

        List<String> unorderedVersions = new ArrayList<String>();
        unorderedVersions.add( "1.3.0-SNAPSHOT" );
        unorderedVersions.add( "1.2.0-SNAPSHOT" );
        unorderedVersions.add( "1.2.0.5-SNAPSHOT" );
        unorderedVersions.add( "1.0.1" );
        unorderedVersions.add( "1.0.3-SNAPSHOT" );
        unorderedVersions.add( "1.1-M1" );
        unorderedVersions.add( "1.0.0-alpha-5" );
        unorderedVersions.add( "1.2.0" );
        unorderedVersions.add( "1.2.0-beta-1" );
        unorderedVersions.add( "1.0.0" );
        unorderedVersions.add( "1.0.0-beta-3" );
        unorderedVersions.add( "1.0.0-beta-4" );
        unorderedVersions.add( "1.0.0-beta-6-SNAPSHOT" );

        Metadata metadata = new Metadata();
        new ArtifactDirMetadataProcessor( null ).versioning( metadata, unorderedVersions );

        assertEquals( orderedVersions, metadata.getVersioning().getVersions() );

    }

    public void testVersioningSnapshotVersionDirectory()
        throws Exception
    {
        List<String> snapshotArtifacts = new ArrayList<String>();

        snapshotArtifacts.add( "nexus-api-1.2.0-20081022.180215-1.pom" );
        snapshotArtifacts.add( "nexus-api-1.2.0-20081022.182430-2.pom" );
        snapshotArtifacts.add( "nexus-api-1.2.0-20081022.184527-3.tar.gz" );
        snapshotArtifacts.add( "nexus-api-1.2.0-20081023.152127-4.jar" );
        snapshotArtifacts.add( "nexus-api-1.2.0-20081024.111337-23-sources.jar" );
        snapshotArtifacts.add( "nexus-api-1.2.0-20081025.143218-32.pom" );
        snapshotArtifacts.add( "nexus-api-1.2.0-SNAPSHOT.pom" );

        Metadata metadata = new Metadata();
        metadata.setGroupId( "org.sonatype.nexus" );
        metadata.setArtifactId( "nexus-api" );
        metadata.setVersion( "1.2.0-SNAPSHOT" );
        new VersionDirMetadataProcessor( null ).versioning( metadata, toGavs( snapshotArtifacts ) );

        assertEquals( "20081025.143218", metadata.getVersioning().getSnapshot().getTimestamp() );
        assertEquals( 32, metadata.getVersioning().getSnapshot().getBuildNumber() );

        List<SnapshotVersion> snapshots = metadata.getVersioning().getSnapshotVersions();
        assertEquals( 4, snapshots.size() );

        for ( SnapshotVersion snap : snapshots )
        {
            if ( snap.getClassifier() == null && "pom".equals( snap.getExtension() ) )
            {
                assertEquals( "20081025143218", snap.getUpdated() );
                assertEquals( "1.2.0-20081025.143218-32", snap.getVersion() );
            }
            else if ( snap.getClassifier() == null && "jar".equals( snap.getExtension() ) )
            {
                assertEquals( "20081023152127", snap.getUpdated() );
                assertEquals( "1.2.0-20081023.152127-4", snap.getVersion() );
            }
            else if ( snap.getClassifier() == null && "tar.gz".equals( snap.getExtension() ) )
            {
                assertEquals( "20081022184527", snap.getUpdated() );
                assertEquals( "1.2.0-20081022.184527-3", snap.getVersion() );
            }
            else if ( "sources".equals( snap.getClassifier() ) && "jar".equals( snap.getExtension() ) )
            {
                assertEquals( "20081024111337", snap.getUpdated() );
                assertEquals( "1.2.0-20081024.111337-23", snap.getVersion() );
            }
            else
            {
                fail( "Unexpected e:" + snap.getExtension() + " c:" + snap.getClassifier() );
            }
        }
    }

    private Collection<Gav> toGavs( List<String> items )
        throws Exception
    {
        String path = "org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/";
        M2GavCalculator calc = new M2GavCalculator();

        List<Gav> gavs = new ArrayList<Gav>();
        for ( String item : items )
        {
            gavs.add( calc.pathToGav( path + item ) );
        }

        return gavs;
    }

}
