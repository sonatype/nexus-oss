package org.sonatype.nexus.maven.tasks;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.DirectoryWalkListener;
import org.codehaus.plexus.util.DirectoryWalker;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.DefaultNexus;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.ItemNotFoundException;
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

        getContainer().getLoggerManager().setThresholds( Logger.LEVEL_DEBUG );

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
        final File sourceSnapshotsRoot = new File( getBasedir(), "src/test/resources/reposes/snapshots" )
            .getAbsoluteFile();

        final URL snapshotsRootUrl = new URL( snapshots.getLocalUrl() );

        final File snapshotsRoot = new File( snapshotsRootUrl.toURI() ).getAbsoluteFile();

        copyDirectory( sourceSnapshotsRoot, snapshotsRoot );

        final File sourceReleasesRoot = new File( getBasedir(), "src/test/resources/reposes/releases" );

        final URL releaseRootUrl = new URL( releases.getLocalUrl() );

        final File releasesRoot = new File( releaseRootUrl.toURI() );

        copyDirectory( sourceReleasesRoot, releasesRoot );

        // This above is possible, since SnapshotRemover is not using index, hence we can manipulate the content
        // "from behind"

        // but clear caches
        snapshots.clearCaches( RepositoryItemUid.PATH_ROOT );
        releases.clearCaches( RepositoryItemUid.PATH_ROOT );
    }

    protected void copyDirectory( final File from, final File to )
        throws IOException
    {
        DirectoryWalker w = new DirectoryWalker();

        w.setBaseDir( from );

        w.addSCMExcludes();

        w.addDirectoryWalkListener( new DirectoryWalkListener()
        {
            public void debug( String message )
            {
            }

            public void directoryWalkStarting( File basedir )
            {
            }

            public void directoryWalkStep( int percentage, File file )
            {
                if ( !file.isFile() )
                {
                    return;
                }

                try
                {
                    String path = file.getAbsolutePath().substring( from.getAbsolutePath().length() );

                    FileUtils.copyFile( file, new File( to, path ) );
                }
                catch ( IOException e )
                {
                    throw new IllegalStateException( "Cannot copy dirtree.", e );
                }
            }

            public void directoryWalkFinished()
            {
            }
        } );

        w.scan();
    }

    protected void validateResults( Map<String, Boolean> results )
        throws Exception
    {
        for ( Map.Entry<String, Boolean> entry : results.entrySet() )
        {
            try
            {
                snapshots.retrieveItem( true, snapshots.createUid( entry.getKey() ), null );

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
