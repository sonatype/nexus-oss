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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;

/**
 * @author juven
 */
public class DefaultSnapshotRemoverTest
    extends AbstractMavenRepoContentTests
{
    protected void validateResults( MavenRepository repository, Map<String, Boolean> results )
        throws Exception
    {
        for ( Map.Entry<String, Boolean> entry : results.entrySet() )
        {
            try
            {
                ResourceStoreRequest request = new ResourceStoreRequest( entry.getKey() );

                repository.retrieveItem( false, request );

                // we succeeded, the value must be true
                assertTrue(
                            "The entry '" + entry.getKey() + "' was found in repository '" + repository.getId() + "' !",
                            entry.getValue() );
            }
            catch ( ItemNotFoundException e )
            {
                // we succeeded, the value must be true
                assertFalse( "The entry '" + entry.getKey() + "' was not found in repository '" + repository.getId()
                    + "' !", entry.getValue() );
            }
        }
    }

    public void testNexus2234()
        throws Exception
    {
        fillInRepo();

        long tenDaysAgo = System.currentTimeMillis() - 10 * 86400000L;

        final URL snapshotsRootUrl = new URL( snapshots.getLocalUrl() );

        final File snapshotsRoot = new File( snapshotsRootUrl.toURI() ).getAbsoluteFile();

        File itemFile =
            new File( snapshotsRoot,
                      "/org/nonuniquesnapgroup/nonuniquesnap/1.1-SNAPSHOT/nonuniquesnap-1.1-SNAPSHOT.jar" );

        itemFile.setLastModified( tenDaysAgo );

        SnapshotRemovalRequest snapshotRemovalRequest =
            new SnapshotRemovalRequest( snapshots.getId(), null, 1, 10, true );

        assertTrue( itemFile.exists() );

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( snapshotRemovalRequest );

        assertTrue( result.isSuccessful() );

        assertTrue( itemFile.exists() );
    }

    /**
     * @see <a href='https://issues.sonatype.org/browse/NEXUS-1331'>https://issues.sonatype.org/browse/NEXUS-1331</a>
     * @throws Exception
     */
    public void testNexus1331()
        throws Exception
    {
        fillInRepo();

        repositoryRegistry.getRepository( "central" ).setLocalStatus( LocalStatus.OUT_OF_SERVICE );

        nexusConfiguration.saveConfiguration();

        // ---------------------------------
        // make the jar should be deleted, while the pom should be kept
        long threeDayAgo = System.currentTimeMillis() - 3 * 86400000L;

        final URL snapshotsRootUrl = new URL( snapshots.getLocalUrl() );

        final File snapshotsRoot = new File( snapshotsRootUrl.toURI() ).getAbsoluteFile();

        File itemFile =
            new File( snapshotsRoot,
                      "/org/sonatype/nexus/nexus-indexer/1.0-beta-3-SNAPSHOT/nexus-indexer-1.0-beta-3-SNAPSHOT.jar" );

        itemFile.setLastModified( threeDayAgo );
        // -----------------------------

        SnapshotRemovalRequest snapshotRemovalRequest =
            new SnapshotRemovalRequest( snapshots.getId(), null, 3, 1, true );

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( snapshotRemovalRequest );

        assertTrue( result.isSuccessful() );

    }

    public void testSnapshotRemoverRemoveReleased()
        throws Exception
    {
        fillInRepo();

        // XXX: the test stuff is published on sonatype, so put the real central out of service for test
        repositoryRegistry.getRepository( "central" ).setLocalStatus( LocalStatus.OUT_OF_SERVICE );

        nexusConfiguration.saveConfiguration();

        // and now setup the request
        // process the apacheSnapshots, leave min 1 snap, remove older than 0 day and delete them if release exists
        SnapshotRemovalRequest snapshotRemovalRequest =
            new SnapshotRemovalRequest( snapshots.getId(), null, 1, 0, true );

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
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.pom",
                       Boolean.FALSE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.jar",
                       Boolean.FALSE );

        // 1.0-beta-5-SNAPSHOT should have only one snapshot remaining, the newest
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.jar",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.jar.sha1",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.pom",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.pom.sha1",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.jar",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.jar.sha1",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.pom",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.pom.sha1",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.jar",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.jar.sha1",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.pom",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.pom.sha1",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.jar",
                       Boolean.TRUE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.jar.sha1",
                       Boolean.TRUE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.pom",
                       Boolean.TRUE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.pom.sha1",
                       Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/maven-metadata.xml", Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/maven-metadata.xml.sha1", Boolean.TRUE );

        validateResults( snapshots, expecting );
    }

    public void testSnapshotRemoverDoNotRemoveReleased()
        throws Exception
    {
        fillInRepo();

        // and now setup the request
        // process the apacheSnapshots, leave min 2 snap, do not remove released ones
        SnapshotRemovalRequest snapshotRemovalRequest =
            new SnapshotRemovalRequest( snapshots.getId(), null, 2, 0, false );

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
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.pom",
                       Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.jar",
                       Boolean.TRUE );

        // 1.0-beta-5-SNAPSHOT should have only two snapshot remaining, the two newest
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.jar",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.pom",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.jar",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.pom",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.jar",
                       Boolean.TRUE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.pom",
                       Boolean.TRUE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.jar",
                       Boolean.TRUE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.pom",
                       Boolean.TRUE );

        validateResults( snapshots, expecting );
    }

    /**
     * Never touch maven metadata files in proxy repo
     * 
     * @throws Exception
     */
    public void testProxyRepo()
        throws Exception
    {
        fillInRepo();

        SnapshotRemovalRequest request = new SnapshotRemovalRequest( apacheSnapshots.getId(), null, 2, 0, false );

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( request );

        assertTrue( result.isSuccessful() );

        HashMap<String, Boolean> expecting = new HashMap<String, Boolean>();
        expecting.put( "org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090209.062729-356.pom", Boolean.TRUE );
        expecting.put( "org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090209.062729-356.pom.md5", Boolean.FALSE );
        expecting.put( "org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090210.090218-375.pom", Boolean.TRUE );
        expecting.put( "org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090202.142204-272.pom", Boolean.FALSE );
        expecting.put( "org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090123.170636-198.pom", Boolean.FALSE );
        expecting.put( "org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090123.160704-197.pom", Boolean.FALSE );
        expecting.put( "org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/maven-metadata.xml", Boolean.FALSE );

        validateResults( apacheSnapshots, expecting );
    }

    /**
     * When there are snapshot files and the metadata file is correct
     * 
     * @throws Exception
     */
    public void testHostedRepoWithMdCorrect()
        throws Exception
    {
        fillInRepo();

        Metadata mdBefore =
            readMavenMetadata( retrieveFile( snapshots, "org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/maven-metadata.xml" ) );

        SnapshotRemovalRequest request = new SnapshotRemovalRequest( snapshots.getId(), null, 1, 0, false );
        SnapshotRemovalResult result = defaultNexus.removeSnapshots( request );

        assertTrue( result.isSuccessful() );

        Metadata mdAfter =
            readMavenMetadata( retrieveFile( snapshots, "org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/maven-metadata.xml" ) );

        assertEquals( mdBefore.getVersioning().getLastUpdated(), mdAfter.getVersioning().getLastUpdated() );
        assertEquals( mdBefore.getVersioning().getSnapshot().getTimestamp(),
                      mdAfter.getVersioning().getSnapshot().getTimestamp() );
        assertEquals( mdBefore.getVersioning().getSnapshot().getBuildNumber(),
                      mdAfter.getVersioning().getSnapshot().getBuildNumber() );

    }

    /**
     * When all the snapshot files are removed, but there's other version
     * 
     * @throws Exception
     */
    public void testHostedRepoWithMdRemoved1()
        throws Exception
    {
        fillInRepo();

        SnapshotRemovalRequest request = new SnapshotRemovalRequest( snapshots.getId(), null, 0, 1, false );
        SnapshotRemovalResult result = defaultNexus.removeSnapshots( request );

        assertTrue( result.isSuccessful() );

        HashMap<String, Boolean> expecting = new HashMap<String, Boolean>();
        // whole version folder was removed, including version metadata
        expecting.put( "org/sonatype/nexus/nexus/1.3.0-SNAPSHOT", Boolean.FALSE );
        expecting.put( "org/sonatype/nexus/nexus/1.2.2-SNAPSHOT", Boolean.TRUE );
        // artifact metadata does exist
        expecting.put( "org/sonatype/nexus/nexus/maven-metadata.xml", Boolean.TRUE );

        validateResults( snapshots, expecting );

        Metadata md = readMavenMetadata( retrieveFile( snapshots, "org/sonatype/nexus/nexus/maven-metadata.xml" ) );
        assertFalse( "The artifact metadata should not contain the removed version!",
                     md.getVersioning().getVersions().contains( "1.3.0-SNAPSHOT" ) );
    }

    /**
     * When all the snapshot files are removed, and all versions are removed
     * 
     * @throws Exception
     */
    public void testHostedRepoWithMdRemoved2()
        throws Exception
    {
        fillInRepo();

        SnapshotRemovalRequest request = new SnapshotRemovalRequest( snapshots.getId(), null, 0, 0, false );
        SnapshotRemovalResult result = defaultNexus.removeSnapshots( request );

        assertTrue( result.isSuccessful() );

        HashMap<String, Boolean> expecting = new HashMap<String, Boolean>();
        // whole version folder was removed, including version metadata
        expecting.put( "org/sonatype/nexus/nexus/1.3.0-SNAPSHOT", Boolean.FALSE );

        // This is no longer valid, since we will NEVER remove non-timestamped artifacts
        // Unless a release version is found, and remove if released is in effect
        // so changed from FALSE to TRUE
        expecting.put( "org/sonatype/nexus/nexus/1.2.2-SNAPSHOT", Boolean.TRUE );
        expecting.put( "org/sonatype/nexus/nexus/maven-metadata.xml", Boolean.TRUE );

        validateResults( snapshots, expecting );
    }

    /**
     * When the metadata is incorrect, fix it
     */
    public void testHostedRepoWithMdIncorrect()
        throws Exception
    {
        fillInRepo();

        SnapshotRemovalRequest request = new SnapshotRemovalRequest( snapshots.getId(), null, 2, 0, false );
        SnapshotRemovalResult result = defaultNexus.removeSnapshots( request );

        assertTrue( result.isSuccessful() );

        HashMap<String, Boolean> expecting = new HashMap<String, Boolean>();

        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-3-SNAPSHOT/maven-metadata.xml", Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-3-SNAPSHOT/maven-metadata.xml.sha1", Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-3-SNAPSHOT/maven-metadata.xml.md5", Boolean.TRUE );

        validateResults( snapshots, expecting );

        Metadata md =
            readMavenMetadata( retrieveFile( snapshots,
                                             "org/sonatype/nexus/nexus-indexer/1.0-beta-3-SNAPSHOT/maven-metadata.xml" ) );

        Assert.assertEquals( 2, md.getVersioning().getSnapshot().getBuildNumber() );
        Assert.assertEquals( "20010711.162119", md.getVersioning().getSnapshot().getTimestamp() );
    }

    /**
     * When the metadata is missing, fix it
     * 
     * @throws Exception
     */
    public void testHostedRepoWithMdMissing()
        throws Exception
    {
        fillInRepo();

        SnapshotRemovalRequest request = new SnapshotRemovalRequest( snapshots.getId(), null, 1, 0, false );
        SnapshotRemovalResult result = defaultNexus.removeSnapshots( request );

        assertTrue( result.isSuccessful() );

        HashMap<String, Boolean> expecting = new HashMap<String, Boolean>();

        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/maven-metadata.xml", Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/maven-metadata.xml.sha1", Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/maven-metadata.xml.md5", Boolean.TRUE );

        validateResults( snapshots, expecting );

    }

    public void testMinToKeep()
        throws Exception
    {
        fillInRepo();

        SnapshotRemovalRequest request = new SnapshotRemovalRequest( snapshots.getId(), null, 1, 1, false );
        SnapshotRemovalResult result = defaultNexus.removeSnapshots( request );

        assertTrue( result.isSuccessful() );

        HashMap<String, Boolean> expecting = new HashMap<String, Boolean>();
        expecting.put( "/org/sonatype/nexus/nexus/1.2.2-SNAPSHOT/nexus-1.2.2-20080123.160704-197.pom", Boolean.FALSE );
        expecting.put( "/org/sonatype/nexus/nexus/1.2.2-SNAPSHOT/nexus-1.2.2-SNAPSHOT.pom", Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090123.160704-197.pom", Boolean.FALSE );
        expecting.put( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090123.170636-198.pom", Boolean.FALSE );
        expecting.put( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090202.142204-272.pom", Boolean.FALSE );
        expecting.put( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090209.062729-356.pom", Boolean.FALSE );
        expecting.put( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090210.090218-375.pom", Boolean.TRUE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.pom",
                       Boolean.TRUE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.pom",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.jar",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.pom",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.jar",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.pom",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.jar",
                       Boolean.TRUE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.pom",
                       Boolean.TRUE );

        validateResults( snapshots, expecting );
    }

    public void testGroup()
        throws Exception
    {
        fillInRepo();

        // run on the public group, which contains the snapshot repo
        SnapshotRemovalRequest request = new SnapshotRemovalRequest( null, "public", 1, 1, false );
        SnapshotRemovalResult result = defaultNexus.removeSnapshots( request );

        assertTrue( result.isSuccessful() );

        HashMap<String, Boolean> expecting = new HashMap<String, Boolean>();
        expecting.put( "/org/sonatype/nexus/nexus/1.2.2-SNAPSHOT/nexus-1.2.2-20080123.160704-197.pom", Boolean.FALSE );
        expecting.put( "/org/sonatype/nexus/nexus/1.2.2-SNAPSHOT/nexus-1.2.2-SNAPSHOT.pom", Boolean.TRUE );
        expecting.put( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090123.160704-197.pom", Boolean.FALSE );
        expecting.put( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090123.170636-198.pom", Boolean.FALSE );
        expecting.put( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090202.142204-272.pom", Boolean.FALSE );
        expecting.put( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090209.062729-356.pom", Boolean.FALSE );
        expecting.put( "/org/sonatype/nexus/nexus/1.3.0-SNAPSHOT/nexus-1.3.0-20090210.090218-375.pom", Boolean.TRUE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.pom",
                       Boolean.TRUE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080711.162119-2.pom",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.jar",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080718.231118-50.pom",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.jar",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080730.002543-149.pom",
                       Boolean.FALSE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.jar",
                       Boolean.TRUE );
        expecting.put(
                       "/org/sonatype/nexus/nexus-indexer/1.0-beta-5-SNAPSHOT/nexus-indexer-1.0-beta-5-20080731.150252-163.pom",
                       Boolean.TRUE );

        validateResults( snapshots, expecting );
    }

    public void testContinueOnException()
        throws Exception
    {
        fillInRepo();

        SnapshotRemovalRequest request = new SnapshotRemovalRequest( snapshots.getId(), null, 0, 0, false );

        assertTrue( defaultNexus.removeSnapshots( request ).isSuccessful() );

        HashMap<String, Boolean> expecting = new HashMap<String, Boolean>();
        expecting.put(
                       "/org/myorg/very.very.long.project.id/1.1-SNAPSHOT/very.very.long.project.id-1.1-20070807.081844-1.jar",
                       Boolean.FALSE );
        expecting.put(
                       "/org/myorg/very.very.long.project.id/1.0.0-SNAPSHOT/1.0.0-SNAPSHOT/very.very.long.project.id-1.0.0-20070807.081844-1.jar",
                       Boolean.FALSE );
        validateResults( snapshots, expecting );

        // we could not retrieve the illegal artifact, but we can check the file system
        File snapshotsStorageBase = new File( WORK_HOME, "storage/" + snapshots.getId() );
        File illegalArtifact =
            new File(
                      snapshotsStorageBase,
                      "org/myorg/very.very.long.project.id/1.0.0-SNAPSHOT/1.0.0-SNAPSHOT/very.very.long.project.id-1.0.0-20070807.081844-1.jar" );
        assertTrue( illegalArtifact.exists() );
    }

    private Metadata readMavenMetadata( File mdFle )
        throws FileNotFoundException, MetadataException
    {
        FileInputStream inputStream = new FileInputStream( mdFle );
        Metadata md = null;

        try
        {
            md = MetadataBuilder.read( inputStream );
        }
        finally
        {
            if ( inputStream != null )
            {
                try
                {
                    inputStream.close();
                }
                catch ( IOException e1 )
                {
                }
            }
        }
        return md;
    }

    public void testKeepAllBeforeReleased()
        throws Exception
    {
        File source = new File( getBasedir(), "src/test/resources/reposes/keepall/snapshots" ).getAbsoluteFile();
        File dest = new File( new URL( snapshots.getLocalUrl() ).toURI() ).getAbsoluteFile();
        copyDirectory( source, dest );

        // XXX: the test stuff is published on sonatype, so put the real central out of service for test
        repositoryRegistry.getRepository( "central" ).setLocalStatus( LocalStatus.OUT_OF_SERVICE );

        nexusConfiguration.saveConfiguration();

        HashMap<String, Boolean> expecting = new HashMap<String, Boolean>();

        expecting.put( "/nexus634/artifact/1.0-SNAPSHOT/artifact-1.0-20010101.184024-1.jar", Boolean.TRUE );
        expecting.put( "/nexus634/artifact/1.0-SNAPSHOT/artifact-1.0-SNAPSHOT.jar", Boolean.TRUE );
        expecting.put( "/nexus634/artifact/1.0-SNAPSHOT/artifact-1.0-SNAPSHOT.pom", Boolean.TRUE );

        validateResults( snapshots, expecting );

        // minCountOfSnapshotsToKeep infinite
        SnapshotRemovalRequest snapshotRemovalRequest =
            new SnapshotRemovalRequest( snapshots.getId(), null, -1, 0, false );

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( snapshotRemovalRequest );
        assertEquals( 1, result.getProcessedRepositories().size() );
        assertTrue( result.isSuccessful() );
        validateResults( snapshots, expecting );

        // removeSnapshotsOlderThanDays infinite
        snapshotRemovalRequest = new SnapshotRemovalRequest( snapshots.getId(), null, 0, -1, false );

        result = defaultNexus.removeSnapshots( snapshotRemovalRequest );
        assertEquals( 1, result.getProcessedRepositories().size() );
        assertTrue( result.isSuccessful() );
        validateResults( snapshots, expecting );

        // remove if release (not released yet)
        snapshotRemovalRequest = new SnapshotRemovalRequest( snapshots.getId(), null, -1, -1, true );

        result = defaultNexus.removeSnapshots( snapshotRemovalRequest );
        assertEquals( 1, result.getProcessedRepositories().size() );
        assertTrue( result.isSuccessful() );
        validateResults( snapshots, expecting );

        // release
        File releaseSource = new File( getBasedir(), "src/test/resources/reposes/keepall/releases" ).getAbsoluteFile();
        File releaseDest = new File( new URL( this.releases.getLocalUrl() ).toURI() ).getAbsoluteFile();
        copyDirectory( releaseSource, releaseDest );

        // remove if release (released now)
        snapshotRemovalRequest = new SnapshotRemovalRequest( snapshots.getId(), null, -1, -1, true );

        result = defaultNexus.removeSnapshots( snapshotRemovalRequest );
        assertEquals( 1, result.getProcessedRepositories().size() );
        assertTrue( result.isSuccessful() );

        expecting.put( "/nexus634/artifact/1.0-SNAPSHOT/artifact-1.0-20010101.184024-1.jar", Boolean.FALSE );
        expecting.put( "/nexus634/artifact/1.0-SNAPSHOT/artifact-1.0-SNAPSHOT.jar", Boolean.FALSE );
        expecting.put( "/nexus634/artifact/1.0-SNAPSHOT/artifact-1.0-SNAPSHOT.pom", Boolean.FALSE );

        validateResults( snapshots, expecting );
    }
}
