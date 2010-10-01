package org.sonatype.nexus.index.locator;

import java.io.File;

import org.sonatype.nexus.artifact.ArtifactPackagingMapper;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.artifact.M2GavCalculator;
import org.sonatype.nexus.index.AbstractNexusIndexerTest;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactContextProducer;
import org.sonatype.nexus.index.NexusIndexer;

public class ArtifactLocatorTest
    extends AbstractNexusIndexerTest
{
    protected File repo = new File( getBasedir(), "src/test/repo" );

    private ArtifactContextProducer artifactContextProducer;

    private ArtifactPackagingMapper artifactPackagingMapper;

    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context = nexusIndexer.addIndexingContext( "al-test", "al-test", repo, indexDir, null, null, FULL_CREATORS );

        nexusIndexer.scan( context );

        artifactContextProducer = lookup( ArtifactContextProducer.class );

        artifactPackagingMapper = lookup( ArtifactPackagingMapper.class );
    }

    public void testContextProducer()
        throws IllegalArtifactCoordinateException
    {
        final File pomFile =
            getTestFile( "src/test/repo/ch/marcus-schulte/maven/hivedoc-plugin/1.0.0/hivedoc-plugin-1.0.0.pom" );

        final ArtifactContext ac = artifactContextProducer.getArtifactContext( context, pomFile );

        assertTrue( "Artifact file was not found!", ac.getArtifact() != null );
        assertTrue( "Artifact file was not found!", ac.getArtifact().exists() );
    }

    public void testArtifactLocator()
        throws IllegalArtifactCoordinateException
    {
        ArtifactLocator al = new ArtifactLocator( artifactPackagingMapper );

        final M2GavCalculator gavCalculator = new M2GavCalculator();

        final File pomFile =
            getTestFile( "src/test/repo/ch/marcus-schulte/maven/hivedoc-plugin/1.0.0/hivedoc-plugin-1.0.0.pom" );

        final Gav gav =
            gavCalculator.pathToGav( "/ch/marcus-schulte/maven/hivedoc-plugin/1.0.0/hivedoc-plugin-1.0.0.pom" );

        File artifactFile = al.locate( pomFile, gavCalculator, gav );

        assertTrue( "Artifact file was not located!", artifactFile != null );
        assertTrue( "Artifact file was not located!", artifactFile.exists() );
    }
}
