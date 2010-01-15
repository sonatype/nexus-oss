package org.sonatype.nexus.index.updater;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.index.AbstractIndexCreatorHelper;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.index.packer.IndexPackingRequest;

public abstract class AbstractIndexUpdaterTest
    extends AbstractIndexCreatorHelper
{
    File testBasedir ;

    String repositoryId = "test";

    String repositoryUrl = "http://repo1.maven.org/maven2/";

    NexusIndexer indexer;

    IndexUpdater updater;

    IndexPacker packer;

    IndexingContext context;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        testBasedir = new File( getBasedir() , "/target/indexUpdater" );
        testBasedir.mkdirs();

        indexer = lookup( NexusIndexer.class );

        updater = lookup( IndexUpdater.class );

        packer = lookup( IndexPacker.class );

        Directory indexDirectory = new RAMDirectory();

        context = indexer.addIndexingContext(
            repositoryId,
            repositoryId,
            null,
            indexDirectory,
            repositoryUrl,
            null,
            MIN_CREATORS );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        FileUtils.forceDelete( testBasedir );
    }
    
    
    protected ArtifactContext createArtifactContext( String repositoryId, String groupId, String artifactId,
        String version, String classifier ) throws IllegalArtifactCoordinateException
    {
        String path = createPath( groupId, artifactId, version, classifier );
        File pomFile = new File( path + ".pom" );
        File artifact = new File( path + ".jar" );
        File metadata = null;
        ArtifactInfo artifactInfo = new ArtifactInfo( repositoryId, groupId, artifactId, version, classifier );
        Gav gav = new Gav(
            groupId,
            artifactId,
            version,
            classifier,
            "jar",
            null,
            null,
            artifact.getName(),
            false,
            false,
            null,
            false,
            null );
        return new ArtifactContext( pomFile, artifact, metadata, artifactInfo, gav );
    }

    protected String createPath( String groupId, String artifactId, String version, String classifier )
    {
        return "/" + groupId + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version
            + ( classifier == null ? "" : "-" + classifier );
    }

    protected void packIndex( File targetDir, IndexingContext context )
        throws IllegalArgumentException, IOException
    {
        IndexPackingRequest request = new IndexPackingRequest( context, targetDir );
        request.setUseTargetProperties( true );
        packer.packIndex( request );
    }

}
