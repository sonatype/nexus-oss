package org.sonatype.nexus.index.cli;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.lucene.search.Query;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;

public abstract class AbstractNexusIndexerCliTest
    extends PlexusTestCase
{

    private static final String DEST_DIR = new File( "target/clitest/output" ).getAbsolutePath();

    private static final String INDEX_DIR = new File( "target/clitest/index" ).getAbsolutePath();

    private static final String TEST_REPO = new File( "src/test/repo" ).getAbsolutePath();

    protected OutputStream out;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        out = new OutputStream()
        {

            private StringBuffer buf = new StringBuffer();

            @Override
            public void write( int b )
                throws IOException
            {
                byte[] bytes = new byte[1];
                bytes[0] = (byte) b;
                buf.append( new String( bytes ) );
            }

            @Override
            public String toString()
            {
                String string = buf.toString();
                buf = new StringBuffer();
                return string;
            }
        };

        FileUtils.deleteDirectory( INDEX_DIR );
        FileUtils.deleteDirectory( DEST_DIR );

    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

    }

    public void testNoArgs()
    {
        int code = execute();
        String output = out.toString();
        assertEquals( output, 1, code );
        assertTrue( "Should print usage", output.contains( "usage: nexus-indexer [options]" ) );
    }

    public void testRequiredArgs()
        throws Exception
    {
        int code = execute( "--repository", TEST_REPO, "--index", INDEX_DIR, "-d", DEST_DIR );
        String output = out.toString();
        assertEquals( output, 0, code );
        assertIndexFiles();
    }

    public void testMissingArgs()
        throws IOException
    {
        String usage = "usage: nexus-indexer";

        int code = execute( "--repository", "--index", INDEX_DIR, "-d", DEST_DIR );
        String output = out.toString();
        assertEquals( output, 1, code );
        assertTrue( "Should print bad usage", output.contains( usage ) );

        code = execute( "--repository", TEST_REPO, "--index", "-d", DEST_DIR );
        output = out.toString();
        assertEquals( output, 1, code );
        assertTrue( "Should print bad usage", output.contains( usage ) );

        code = execute( "--repository", TEST_REPO, "--index", INDEX_DIR, "-d" );
        output = out.toString();
        assertEquals( output, 1, code );
        assertTrue( "Should print bad usage", output.contains( usage ) );

        code = execute( "--repository", "--index", "-d" );
        output = out.toString();
        assertEquals( output, 1, code );
        assertTrue( "Should print bad usage", output.contains( usage ) );

        assertFalse( "No index file was generated", new File( INDEX_DIR ).exists() );
    }

    public void testAbrvsRequiredArgs()
        throws Exception
    {
        int code = execute( "-r", TEST_REPO, "-i", INDEX_DIR, "-d", DEST_DIR );
        String output = out.toString();
        assertEquals( output, 0, code );
        assertIndexFiles();
    }

    public void /* test */LogginLevel()
        throws Exception
    {
        int code = execute( "-r", TEST_REPO, "-i", INDEX_DIR, "-d", DEST_DIR );
        String normal = out.toString();
        assertEquals( normal, 0, code );
        assertIndexFiles();

        setUp();

        code = execute( "-q", "-r", TEST_REPO, "-i", INDEX_DIR, "-d", DEST_DIR );
        String quiet = out.toString();
        assertEquals( quiet, 0, code );
        assertFalse( "Expected an different output on quiet mode:\n" + normal, normal.equals( quiet ) );
        assertIndexFiles();

        setUp();

        code = execute( "-X", "-r", TEST_REPO, "-i", INDEX_DIR, "-d", DEST_DIR );
        String debug = out.toString();
        assertEquals( debug, 0, code );
        assertFalse( "Expected an different output on debug mode:\n" + normal, normal.equals( debug ) );
        assertIndexFiles();

        setUp();

        code = execute( "-e", "-r", TEST_REPO, "-i", INDEX_DIR, "-d", DEST_DIR );
        String error = out.toString();
        assertEquals( error, 0, code );
        assertFalse( "Expected an different output on error mode:\n" + normal, normal.equals( error ) );
        assertIndexFiles();
    }

    public void testInvalidRepo()
        throws Exception
    {
        int code =
            execute( "-r", new File( "target/undexinting/repo/to/try/what/will/happen/here" ).getCanonicalPath(), "-i",
                     INDEX_DIR, "-d", DEST_DIR );
        String output = out.toString();
        assertEquals( output, 1, code );
    }

    private void assertIndexFiles()
        throws Exception
    {
        IndexingContext context = null;
        try
        {
            List<IndexCreator> indexCreators = getContainer().lookupList( IndexCreator.class );

            NexusIndexer indexer = lookup( NexusIndexer.class );
            context =
                indexer.addIndexingContext( "index", "index", new File( TEST_REPO ), new File( INDEX_DIR ), null, null,
                                            indexCreators );

            assertFalse( "No index file was generated", new File( INDEX_DIR ).list().length == 0 );

            Query query = indexer.constructQuery( ArtifactInfo.GROUP_ID, "ch.marcus-schulte.maven" );

            FlatSearchRequest request = new FlatSearchRequest( query );
            FlatSearchResponse response = indexer.searchFlat( request );
            assertEquals( response.getResults().toString(), 1, response.getTotalHits() );
        }
        finally
        {
            if ( context != null )
            {
                context.close( false );
            }
        }
    }

    protected abstract int execute( String... args );

}