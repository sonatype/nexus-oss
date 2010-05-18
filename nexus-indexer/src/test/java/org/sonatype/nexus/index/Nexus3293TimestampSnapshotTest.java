package org.sonatype.nexus.index;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.WildcardQuery;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.M2GavCalculator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;

public class Nexus3293TimestampSnapshotTest
    extends AbstractIndexCreatorHelper
{
    private IndexingContext context;

    private NexusIndexer prepare()
        throws Exception, IOException, UnsupportedExistingLuceneIndexException
    {
        NexusIndexer indexer = lookup( NexusIndexer.class );

        File indexDir = new File( getBasedir(), "target/index/test-" + Long.toString( System.currentTimeMillis() ) );
        FileUtils.deleteDirectory( indexDir );

        File repo = new File( getBasedir(), "src/test/nexus-3293" );
        repo.mkdirs();

        context = indexer.addIndexingContext( "test", "test", repo, indexDir, null, null, DEFAULT_CREATORS );

        // IndexReader indexReader = context.getIndexSearcher().getIndexReader();
        // int numDocs = indexReader.numDocs();
        // for ( int i = 0; i < numDocs; i++ )
        // {
        // Document doc = indexReader.document( i );
        // System.err.println( i + " : " + doc.get( ArtifactInfo.UINFO));
        //          
        // }
        return indexer;
    }

    public void test_nexus_3293_releaseJar()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        File artifact =
            new File( getBasedir(),
                      "src/test/nexus-3293/aopalliance/aopalliance/1.0/aopalliance-1.0jar" );

        File pom =
            new File( getBasedir(),
                      "src/test/nexus-3293/aopalliance/aopalliance/1.0/aopalliance-1.0.pom" );

        ArtifactInfo artifactInfo = new ArtifactInfo( "test", "aopalliance", "aopalliance", "1.0-SNAPSHOT", null );

        M2GavCalculator gavCalc = new M2GavCalculator();

        Gav jarGav = gavCalc.pathToGav( "aopalliance/aopalliance/1.0/aopalliance-1.0.jar" );
        Gav pomGav = gavCalc.pathToGav( "aopalliance/aopalliance/1.0/aopalliance-1.0.pom" );

        ArtifactContext artifactContext = new ArtifactContext( pom, artifact, null, artifactInfo, jarGav );

        indexer.addArtifactToIndex( artifactContext, context );

        validateIndexContents( indexer );

        artifactContext = new ArtifactContext( pom, artifact, null, artifactInfo, jarGav );

        indexer.addArtifactToIndex( artifactContext, context );

        validateIndexContents( indexer );
    }

    public void test_nexus_3293_indexTimestampedSnapshotJar()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        File artifact =
            new File( getBasedir(),
                      "src/test/nexus-3293/aopalliance/aopalliance/1.0-SNAPSHOT/aopalliance-1.0-20100517.210215-13.jar" );

        File pom =
            new File( getBasedir(),
                      "src/test/nexus-3293/aopalliance/aopalliance/1.0-SNAPSHOT/aopalliance-1.0-20100517.210215-13.pom" );

        
        ArtifactContextProducer artifactContextProducer = lookup( ArtifactContextProducer.class );
        
        ArtifactContext artifactContext = artifactContextProducer.getArtifactContext( context, artifact );

        indexer.addArtifactToIndex( artifactContext, context );

        validateIndexContents( indexer );

        artifactContext = artifactContextProducer.getArtifactContext( context, pom );

        indexer.addArtifactToIndex( artifactContext, context );

        validateIndexContents( indexer );
    }

    private void validateIndexContents( NexusIndexer indexer )
        throws Exception
    {
        WildcardQuery q = new WildcardQuery( new Term( ArtifactInfo.UINFO, "*aopalliance*" ) );

        FlatSearchRequest request = new FlatSearchRequest( q );
        request.getContexts().add( context );

        FlatSearchResponse response = indexer.searchFlat( request );
        Set<ArtifactInfo> r = response.getResults();
        assertEquals( r.toString(), 1, r.size() );

        ArtifactInfo ai = r.iterator().next();

        assertEquals( "jar", ai.packaging );
        assertEquals( "jar", ai.fextension );
    }
}
