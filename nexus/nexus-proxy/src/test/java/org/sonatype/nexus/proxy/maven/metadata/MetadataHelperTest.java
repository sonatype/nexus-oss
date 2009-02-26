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
package org.sonatype.nexus.proxy.maven.metadata;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.repository.metadata.Metadata;
import org.sonatype.nexus.proxy.maven.metadata.AbstractMetadataHelper;

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
        orderedVersions.add( "1.2.0-SNAPSHOT" );
        orderedVersions.add( "1.2.0-beta-1" );
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

        snapshotArtifacts.add( "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.180215-1.pom" );
        snapshotArtifacts.add( "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.182430-2.pom" );
        snapshotArtifacts.add( "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081022.184527-3.pom" );
        snapshotArtifacts.add( "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-20081025.143218-32.pom" );
        snapshotArtifacts.add( "/org/sonatype/nexus/nexus-api/1.2.0-SNAPSHOT/nexus-api-1.2.0-SNAPSHOT.pom" );

        Metadata metadata = new Metadata();
        metadata.setGroupId( "org.sonatype.nexus" );
        metadata.setArtifactId( "nexus-api" );
        metadata.setVersion( "1.2.0-SNAPSHOT" );
        new VersionDirMetadataProcessor( null ).versioning( metadata, snapshotArtifacts );

        assertEquals( "20081025.143218", metadata.getVersioning().getSnapshot().getTimestamp() );
        assertEquals( 32, metadata.getVersioning().getSnapshot().getBuildNumber() );
    }

}
