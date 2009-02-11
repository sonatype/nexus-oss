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
package org.sonatype.nexus.maven.tasks;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.repository.LocalStatus;

public class DefaultSnapshotRemoverTest
    extends AbstractMavenRepoContentTests
{
    protected void validateResults( Map<String, Boolean> results )
        throws Exception
    {
        for ( Map.Entry<String, Boolean> entry : results.entrySet() )
        {
            try
            {
                snapshots.retrieveItem( snapshots.createUid( entry.getKey() ), null );

                // we succeaeded, the value must be true
                assertTrue( "The entry '" + entry.getKey() + "' was found in repository.", entry.getValue() );
            }
            catch ( ItemNotFoundException e )
            {
                // we succeeded, the value must be true
                assertFalse( "The entry '" + entry.getKey() + "' was not found in repository.", entry.getValue() );
            }
        }
    }
    
    /**
     * @see <a href='https://issues.sonatype.org/browse/NEXUS-1331'>https://issues.sonatype.org/browse/NEXUS-1331</a>
     * @throws Exception
     */
    public void testNexus1331()
        throws Exception
    {
        fillInRepo();

        getNexus().getRepository( "central" ).setLocalStatus( LocalStatus.OUT_OF_SERVICE );

        // ---------------------------------
        // make the jar should be deleted, while the pom should be kept
        long threeDayAgo = System.currentTimeMillis() - 3 * 86400000L;

        final URL snapshotsRootUrl = new URL( snapshots.getLocalUrl() );

        final File snapshotsRoot = new File( snapshotsRootUrl.toURI() ).getAbsoluteFile();

        File itemFile = new File(
            snapshotsRoot,
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-3-SNAPSHOT/nexus-indexer-1.0-beta-3-SNAPSHOT.jar" );

        itemFile.setLastModified( threeDayAgo );
        // -----------------------------

        SnapshotRemovalRequest snapshotRemovalRequest = new SnapshotRemovalRequest( snapshots.getId(), null, 3, 1, true );

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( snapshotRemovalRequest );

        assertTrue( result.isSuccessful() );

    }

    public void testSnapshotRemoverRemoveReleased()
        throws Exception
    {
        fillInRepo();

        // XXX: the test stuff is published on sonatype, so put the real central out of service for test
        getNexus().getRepository( "central" ).setLocalStatus( LocalStatus.OUT_OF_SERVICE );

        // and now setup the request
        // process the apacheSnapshots, leave min 1 snap, remove older than 0 day and delete them if release exists
        SnapshotRemovalRequest snapshotRemovalRequest = new SnapshotRemovalRequest( snapshots.getId(), null, 1, 0, true );

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( snapshotRemovalRequest );

        assertEquals( 1, result.getProcessedRepositories().size() );
        
        assertTrue( result.isSuccessful() );

        HashMap<String, Boolean> expecting = new HashMap<String, Boolean>();

        // 1.0-beta-4-SNAPSHOT should be nuked completely
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT-cli.jar",
            Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT-jdk14.jar",
            Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT-sources.jar",
            Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.pom",
            Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.jar",
            Boolean.FALSE );

        // 1.0-beta-5-SNAPSHOT should have only one snapshot remaining, the newest
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.jar",
            Boolean.FALSE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.jar.sha1",
                Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.pom",
            Boolean.FALSE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.pom.sha1",
                Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.jar",
            Boolean.FALSE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.jar.sha1",
                Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.pom",
            Boolean.FALSE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.pom.sha1",
                Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.jar",
            Boolean.FALSE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.jar.sha1",
                Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.pom",
            Boolean.FALSE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.pom.sha1",
                Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.jar",
            Boolean.TRUE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.jar.sha1",
                Boolean.TRUE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.pom",
            Boolean.TRUE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.pom.sha1",
                Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/maven-metadata.xml", Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/maven-metadata.xml.sha1", Boolean.TRUE );

        validateResults( expecting );
    }

    public void testSnapshotRemoverDoNotRemoveReleased()
        throws Exception
    {
        fillInRepo();

        // and now setup the request
        // process the apacheSnapshots, leave min 2 snap, do not remove released ones
        SnapshotRemovalRequest snapshotRemovalRequest = new SnapshotRemovalRequest(
            snapshots.getId(),
            null,
            2,
            -1,
            false );

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( snapshotRemovalRequest );

        assertEquals( 1, result.getProcessedRepositories().size() );
        
        assertTrue( result.isSuccessful() );

        HashMap<String, Boolean> expecting = new HashMap<String, Boolean>();

        // 1.0-beta-4-SNAPSHOT should be untouched completely
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT-cli.jar",
            Boolean.TRUE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT-jdk14.jar",
            Boolean.TRUE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT-sources.jar",
            Boolean.TRUE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.pom",
            Boolean.TRUE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.jar",
            Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/maven-metadata.xml", Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/maven-metadata.xml.sha1", Boolean.TRUE );

        // 1.0-beta-5-SNAPSHOT should have only twp snapshot remaining, the two newest
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.jar",
            Boolean.FALSE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.jar.sha1",
                Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.pom",
            Boolean.FALSE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.pom.sha1",
                Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.jar",
            Boolean.FALSE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.jar.sha1",
                Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.pom",
            Boolean.FALSE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.pom.sha1",
                Boolean.FALSE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.jar",
            Boolean.TRUE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.jar.sha1",
                Boolean.TRUE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.pom",
            Boolean.TRUE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.pom.sha1",
                Boolean.TRUE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.jar",
            Boolean.TRUE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.jar.sha1",
                Boolean.TRUE );
        expecting.put(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.pom",
            Boolean.TRUE );
        expecting
            .put(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.pom.sha1",
                Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/maven-metadata.xml", Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/maven-metadata.xml.sha1", Boolean.TRUE );

        validateResults( expecting );
    }
}
