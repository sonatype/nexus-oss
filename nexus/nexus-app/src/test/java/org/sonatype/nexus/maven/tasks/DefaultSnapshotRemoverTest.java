package org.sonatype.nexus.maven.tasks;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.DefaultNexus;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;

public class DefaultSnapshotRemoverTest
    extends AbstractNexusTestCase
{
    private DefaultNexus defaultNexus;

    private MavenRepository apacheSnapshots;

    private MavenRepository central;

    private ServletServer servetServer;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        // jetty as "remote repo"
        servetServer = (ServletServer) lookup( ServletServer.ROLE );

        servetServer.start();

        defaultNexus = (DefaultNexus) lookup( Nexus.ROLE );

        apacheSnapshots = (MavenRepository) defaultNexus.getRepository( "apache-snapshots" );

        apacheSnapshots.setRemoteUrl( servetServer.getUrl( apacheSnapshots.getId() ) );

        central = (MavenRepository) defaultNexus.getRepository( "central" );

        central.setRemoteUrl( servetServer.getUrl( central.getId() ) );
    }

    protected void tearDown()
        throws Exception
    {
        servetServer.stop();

        super.tearDown();
    }

    public void fillInRepo()
        throws Exception
    {
        // Case1: different builds of same snap artifact. Should be kept the newest (with regard to params)
        // get 4 pieces of beta-5-snap
        ArtifactStoreRequest gavRequest = new ArtifactStoreRequest(
            "org.sonatype.nexus",
            "nexus-indexer",
            "1.0-beta-5-20080711.162119-2" );

        apacheSnapshots.retrieveArtifactPom( gavRequest );

        apacheSnapshots.retrieveArtifact( gavRequest );

        gavRequest = new ArtifactStoreRequest( "org.sonatype.nexus", "nexus-indexer", "1.0-beta-5-20080718.231118-50" );

        apacheSnapshots.retrieveArtifactPom( gavRequest );

        apacheSnapshots.retrieveArtifact( gavRequest );

        gavRequest = new ArtifactStoreRequest( "org.sonatype.nexus", "nexus-indexer", "1.0-beta-5-20080730.002543-149" );

        apacheSnapshots.retrieveArtifactPom( gavRequest );

        apacheSnapshots.retrieveArtifact( gavRequest );

        gavRequest = new ArtifactStoreRequest( "org.sonatype.nexus", "nexus-indexer", "1.0-beta-5-20080731.150252-163" );

        apacheSnapshots.retrieveArtifactPom( gavRequest );

        apacheSnapshots.retrieveArtifact( gavRequest );

        // Case2: a snap is here that has release also in other repo
        // get 1 piece of nexus-indexer b4
        apacheSnapshots.retrieveItem( new ResourceStoreRequest(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.pom",
            false ) );

        apacheSnapshots.retrieveItem( new ResourceStoreRequest(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.jar",
            false ) );

        // and get it's release counterpart
        central.retrieveItem( new ResourceStoreRequest(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4/nexus-indexer-1.0-beta-4.pom",
            false ) );

        central.retrieveItem( new ResourceStoreRequest(
            "/org/sonatype/nexus/nexus-indexer/1.0-beta-4/nexus-indexer-1.0-beta-4.jar",
            false ) );
    }

    public void testSnapshotRemover()
        throws Exception
    {
        fillInRepo();

        // and now setup the request
        // process the apacheSnapshots, leave min 1 snap, remove older than 0 day and delete them if release exists
        SnapshotRemovalRequest request = new SnapshotRemovalRequest( apacheSnapshots.getId(), null, 1, 0, true );

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( request );

        assertEquals( 1, result.getProcessedRepositories().size() );

        ArtifactStoreRequest gavRequest = null;

        // test the results by their local availability

        gavRequest = new ArtifactStoreRequest( "org.sonatype.nexus", "nexus-indexer", "1.0-beta-5-20080731.150252-163" );

        gavRequest.setRequestLocalOnly( true );

        try
        {
            apacheSnapshots.retrieveArtifactPom( gavRequest );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "This is the newest, it should be left!" );
        }

        try
        {
            apacheSnapshots.retrieveArtifact( gavRequest );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "This is the newest, it should be left!" );
        }

        gavRequest = new ArtifactStoreRequest( "org.sonatype.nexus", "nexus-indexer", "1.0-beta-5-20080730.002543-149" );

        gavRequest.setRequestLocalOnly( true );

        try
        {
            apacheSnapshots.retrieveArtifactPom( gavRequest );

            fail( "This is not the newest, it should be deleted!" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        gavRequest = new ArtifactStoreRequest( "org.sonatype.nexus", "nexus-indexer", "1.0-beta-5-20080718.231118-50" );

        gavRequest.setRequestLocalOnly( true );

        try
        {
            apacheSnapshots.retrieveArtifactPom( gavRequest );

            fail( "This is not the newest, it should be deleted!" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        try
        {
            apacheSnapshots.retrieveArtifact( gavRequest );

            fail( "This is not the newest, it should be deleted!" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        // get 1 piece of maven-artifact
        try
        {
            apacheSnapshots.retrieveItem( new ResourceStoreRequest(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.pom",
                true ) );

            fail( "Release exists, it should be deleted!" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        try
        {
            apacheSnapshots.retrieveItem( new ResourceStoreRequest(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.jar",
                true ) );

            fail( "Release exists, it should be deleted!" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        // and get it's release counterpart
        try
        {
            central.retrieveItem( new ResourceStoreRequest(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-4/nexus-indexer-1.0-beta-4.pom",
                true ) );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "This is the release, it should be left!" );
        }

        try
        {
            central.retrieveItem( new ResourceStoreRequest(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-4/nexus-indexer-1.0-beta-4.jar",
                true ) );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "This is the release, it should be left!" );
        }

        /*******************************************
         * Start round 2
         ******************************************/

        fillInRepo();

        // and now setup the request
        // process the apacheSnapshots, leave min 2 snap
        request = new SnapshotRemovalRequest( apacheSnapshots.getId(), null, 2, -1, false );

        result = defaultNexus.removeSnapshots( request );

        assertEquals( 1, result.getProcessedRepositories().size() );

        gavRequest = null;

        // test the results by their local availability

        gavRequest = new ArtifactStoreRequest( "org.sonatype.nexus", "nexus-indexer", "1.0-beta-5-20080731.150252-163" );

        gavRequest.setRequestLocalOnly( true );

        try
        {
            apacheSnapshots.retrieveArtifactPom( gavRequest );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "Should not be deleted!" );
        }

        try
        {
            apacheSnapshots.retrieveArtifact( gavRequest );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "Should not be deleted!" );
        }

        gavRequest = new ArtifactStoreRequest( "org.sonatype.nexus", "nexus-indexer", "1.0-beta-5-20080730.002543-149" );

        gavRequest.setRequestLocalOnly( true );

        try
        {
            apacheSnapshots.retrieveArtifactPom( gavRequest );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "Should not be deleted!" );
        }

        try
        {
            apacheSnapshots.retrieveArtifact( gavRequest );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "Should not be deleted!" );
        }

        gavRequest = new ArtifactStoreRequest( "org.sonatype.nexus", "nexus-indexer", "1.0-beta-5-20080718.231118-50" );

        gavRequest.setRequestLocalOnly( true );

        try
        {
            apacheSnapshots.retrieveArtifactPom( gavRequest );

            fail( "Should be deleted!" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        try
        {
            apacheSnapshots.retrieveArtifact( gavRequest );

            fail( "Should be deleted!" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        // get 1 piece of maven-artifact
        try
        {
            apacheSnapshots.retrieveItem( new ResourceStoreRequest(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.pom",
                true ) );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "Should not be deleted!" );
        }

        try
        {
            apacheSnapshots.retrieveItem( new ResourceStoreRequest(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-4-SNAPSHOT/nexus-indexer-1.0-beta-4-SNAPSHOT.jar",
                true ) );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "Should not be deleted!" );
        }

        // and get it's release counterpart
        try
        {
            central.retrieveItem( new ResourceStoreRequest(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-4/nexus-indexer-1.0-beta-4.pom",
                true ) );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "Should not be deleted!" );
        }

        try
        {
            central.retrieveItem( new ResourceStoreRequest(
                "/org/sonatype/nexus/nexus-indexer/1.0-beta-4/nexus-indexer-1.0-beta-4.jar",
                true ) );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "Should not be deleted!" );
        }

    }

}
