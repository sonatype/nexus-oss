package org.sonatype.nexus.maven.tasks;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.DefaultNexus;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.MavenRepository;

public class DefaultSnapshotRemoverTest
    extends AbstractNexusTestCase
{
    private DefaultNexus defaultNexus;

    private MavenRepository snapshots;

    private MavenRepository releases;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        defaultNexus = (DefaultNexus) lookup( Nexus.class );

        // get a snapshots hosted repo
        snapshots = (MavenRepository) defaultNexus.getRepository( "snapshots" );

        // get a releases hosted repo
        releases = (MavenRepository) defaultNexus.getRepository( "releases" );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void fillInRepo()
        throws Exception
    {
        URL snapshotsRootUrl = new URL( snapshots.getLocalUrl() );

        File snapshotsRoot = new File( snapshotsRootUrl.toURI() );

        // copy the files to their place
        FileUtils.copyDirectoryStructure(
            new File( getBasedir(), "src/test/resources/reposes/snapshots" ),
            snapshotsRoot );

        URL releaseRootUrl = new URL( releases.getLocalUrl() );

        File releasesRoot = new File( releaseRootUrl.toURI() );

        // copy the files to their place
        FileUtils
            .copyDirectoryStructure( new File( getBasedir(), "src/test/resources/reposes/releases" ), releasesRoot );

        // This above is possible, since SnapshotRemover is not using index, hence we can manipulate the content
        // "from behind"

        // but clear caches
        snapshots.clearCaches( RepositoryItemUid.PATH_ROOT );
        releases.clearCaches( RepositoryItemUid.PATH_ROOT );
    }

    protected void validateResults( Map<String, Boolean> results )
        throws Exception
    {
        for ( Map.Entry<String, Boolean> entry : results.entrySet() )
        {
            try
            {
                snapshots.retrieveItem( new ResourceStoreRequest( entry.getKey(), false ) );

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

    public void testSnapshotRemoverRemoveReleased()
        throws Exception
    {
        fillInRepo();

        // and now setup the request
        // process the apacheSnapshots, leave min 1 snap, remove older than 0 day and delete them if release exists
        SnapshotRemovalRequest snapshotRemovalRequest = new SnapshotRemovalRequest( snapshots.getId(), null, 1, 0, true );

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( snapshotRemovalRequest );

        assertEquals( 1, result.getProcessedRepositories().size() );

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

        validateResults( expecting );
    }
}
