package org.sonatype.nexus.maven.tasks;

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

    protected void setUp()
        throws Exception
    {
        super.setUp();

        defaultNexus = (DefaultNexus) lookup( Nexus.ROLE );

        apacheSnapshots = (MavenRepository) defaultNexus.getRepository( "apache-snapshots" );

        central = (MavenRepository) defaultNexus.getRepository( "central" );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void fillInRepo()
        throws Exception
    {
        // get 4 pieces of maven-core
        ArtifactStoreRequest gavRequest = new ArtifactStoreRequest(
            "org.apache.maven",
            "maven-core",
            "2.0.9-20080306.010327-7" );

        apacheSnapshots.retrieveArtifactPom( gavRequest );

        apacheSnapshots.retrieveArtifact( gavRequest );

        gavRequest = new ArtifactStoreRequest( "org.apache.maven", "maven-core", "2.0.9-20080302.032223-6" );

        apacheSnapshots.retrieveArtifactPom( gavRequest );

        apacheSnapshots.retrieveArtifact( gavRequest );

        gavRequest = new ArtifactStoreRequest( "org.apache.maven", "maven-core", "2.0.9-20080302.014714-5" );

        apacheSnapshots.retrieveArtifactPom( gavRequest );

        apacheSnapshots.retrieveArtifact( gavRequest );

        gavRequest = new ArtifactStoreRequest( "org.apache.maven", "maven-core", "2.0.9-20080215.024846-4" );

        apacheSnapshots.retrieveArtifactPom( gavRequest );

        apacheSnapshots.retrieveArtifact( gavRequest );

        // get 2 pieces of maven-archiver
        gavRequest = new ArtifactStoreRequest( "org.apache.maven", "maven-archiver", "2.3-20080101.212006-10" );

        apacheSnapshots.retrieveArtifactPom( gavRequest );

        apacheSnapshots.retrieveArtifact( gavRequest );

        gavRequest = new ArtifactStoreRequest( "org.apache.maven", "maven-archiver", "2.3-20080101.211719-9" );

        apacheSnapshots.retrieveArtifactPom( gavRequest );

        apacheSnapshots.retrieveArtifact( gavRequest );

        // get 1 piece of maven-artifact
        apacheSnapshots.retrieveItem( new ResourceStoreRequest(
            "/org/apache/maven/maven-artifact/2.0.9-SNAPSHOT/maven-artifact-2.0.9-20080306.011152-9.pom",
            false ) );

        apacheSnapshots.retrieveItem( new ResourceStoreRequest(
            "/org/apache/maven/maven-artifact/2.0.9-SNAPSHOT/maven-artifact-2.0.9-20080306.011152-9.jar",
            false ) );

        // and get it's release counterpart
        central.retrieveItem( new ResourceStoreRequest(
            "/org/apache/maven/maven-artifact/2.0.9/maven-artifact-2.0.9.pom",
            false ) );

        central.retrieveItem( new ResourceStoreRequest(
            "/org/apache/maven/maven-artifact/2.0.9/maven-artifact-2.0.9.jar",
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

        gavRequest = new ArtifactStoreRequest( "org.apache.maven", "maven-core", "2.0.9-20080306.010327-7" );

        gavRequest.setRequestLocalOnly( true );

        // test the results by their local availability
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

        gavRequest = new ArtifactStoreRequest( "org.apache.maven", "maven-core", "2.0.9-20080302.032223-6" );

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

        gavRequest = new ArtifactStoreRequest( "org.apache.maven", "maven-core", "2.0.9-20080302.014714-5" );

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

        gavRequest = new ArtifactStoreRequest( "org.apache.maven", "maven-core", "2.0.9-20080215.024846-4" );

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

        // get 2 pieces of maven-archiver
        gavRequest = new ArtifactStoreRequest( "org.apache.maven", "maven-archiver", "2.3-20080101.212006-10" );

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

        gavRequest = new ArtifactStoreRequest( "org.apache.maven", "maven-archiver", "2.3-20080101.211719-9" );

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
                "/org/apache/maven/maven-artifact/2.0.9-SNAPSHOT/maven-artifact-2.0.9-20080306.011152-9.pom",
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
                "/org/apache/maven/maven-artifact/2.0.9-SNAPSHOT/maven-artifact-2.0.9-20080306.011152-9.jar",
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
                "/org/apache/maven/maven-artifact/2.0.9/maven-artifact-2.0.9.pom",
                true ) );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "This is the release, it should be left!" );
        }

        try
        {
            central.retrieveItem( new ResourceStoreRequest(
                "/org/apache/maven/maven-artifact/2.0.9/maven-artifact-2.0.9.jar",
                true ) );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "This is the release, it should be left!" );
        }

    }

    public void testSnapshotRemoverDefaultValues()
        throws Exception
    {
        fillInRepo();

        // and now setup the request
        // process the apacheSnapshots, leave min 1 snap, remove older than 0 day and delete them if release exists
        SnapshotRemovalRequest request = new SnapshotRemovalRequest( apacheSnapshots.getId(), null, 1, -1, true );

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( request );

        assertEquals( 1, result.getProcessedRepositories().size() );
    }

}
