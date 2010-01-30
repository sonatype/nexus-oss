package org.sonatype.nexus.index.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.fs.Locker;

public class LocalIndexCacheTest
    extends AbstractIndexUpdaterTest
{
    private File remoteRepo;

    private File localCacheDir;

    private File indexDir;

    private IndexingContext tempContext;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        remoteRepo = new File( "target/localcache/remoterepo" ).getCanonicalFile();
        FileUtils.deleteDirectory( remoteRepo );
        remoteRepo.mkdirs();

        localCacheDir = new File( "target/localcache/cache" ).getCanonicalFile();
        FileUtils.deleteDirectory( localCacheDir );
        localCacheDir.mkdirs();

        indexDir = new File( "target/localcache/index" ).getCanonicalFile();
        FileUtils.deleteDirectory( indexDir );
        indexDir.mkdirs();

    }

    @Override
    protected void tearDown()
        throws Exception
    {
        removeTempContext();

        super.tearDown();
    }

    private IndexingContext getNewTempContext()
        throws IOException, UnsupportedExistingLuceneIndexException
    {
        removeTempContext();

        tempContext =
            indexer.addIndexingContext( repositoryId + "temp", repositoryId, null, indexDir, repositoryUrl, null,
                                        MIN_CREATORS );

        return tempContext;
    }

    private void removeTempContext()
        throws IOException
    {
        if ( tempContext != null )
        {
            indexer.removeIndexingContext( tempContext, true );
            tempContext = null;
            FileUtils.cleanDirectory( indexDir );
        }
    }

    public void testBasic()
        throws Exception
    {
        // create initial remote repo index
        indexer.addArtifactToIndex( createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
                                    context );
        packIndex( remoteRepo, context );

        // 
        TrackingFetcher fetcher;
        IndexUpdateRequest updateRequest;
        IndexingContext testContext;

        // initial index download (expected: full index download)
        testContext = getNewTempContext();
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( testContext );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertEquals( 2, fetcher.getRetrievedResources().size() );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.gz" ).exists() );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.properties" ).exists() );
        assertGroupCount( 1, "commons-lang", testContext );

        // update the same index (expected: no index download)
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( testContext );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertEquals( 1, fetcher.getRetrievedResources().size() );
        assertEquals( "nexus-maven-repository-index.properties", fetcher.getRetrievedResources().get( 0 ) );
        assertGroupCount( 1, "commons-lang", testContext );

        // nuke index but keep the cache (expected: no index download)
        testContext = getNewTempContext();
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( testContext );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertEquals( 1, fetcher.getRetrievedResources().size() );
        assertEquals( "nexus-maven-repository-index.properties", fetcher.getRetrievedResources().get( 0 ) );
        assertGroupCount( 1, "commons-lang", testContext );

        // incremental remote update
        indexer.addArtifactToIndex( createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.3", null ),
                                    context );
        packIndex( remoteRepo, context );

        // update via cache (expected: incremental chunk download)
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( testContext );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertEquals( 2, fetcher.getRetrievedResources().size() );
        assertEquals( "nexus-maven-repository-index.properties", fetcher.getRetrievedResources().get( 0 ) );
        assertEquals( "nexus-maven-repository-index.1.gz", fetcher.getRetrievedResources().get( 1 ) );
        assertGroupCount( 2, "commons-lang", testContext );

        // nuke index but keep the cache (expected: no index download, index contains both initial and delta chunks)
        testContext = getNewTempContext();
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( testContext );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertEquals( 1, fetcher.getRetrievedResources().size() );
        assertEquals( "nexus-maven-repository-index.properties", fetcher.getRetrievedResources().get( 0 ) );
        assertGroupCount( 2, "commons-lang", testContext );

        // kill the cache, but keep the index (expected: full index download)
        // TODO how to assert if merge==false internally?
        FileUtils.deleteDirectory( localCacheDir );
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( testContext );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertEquals( 2, fetcher.getRetrievedResources().size() );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.gz" ).exists() );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.properties" ).exists() );
        assertGroupCount( 2, "commons-lang", testContext );
    }

    private void assertGroupCount( int expectedCount, String groupId, IndexingContext context )
        throws IOException
    {
        TermQuery query = new TermQuery( new Term( ArtifactInfo.GROUP_ID, groupId ) );
        FlatSearchResponse response = indexer.searchFlat( new FlatSearchRequest( query, context ) );
        assertEquals( expectedCount, response.getTotalHits() );
    }

    public void testForceIndexDownload()
        throws Exception
    {
        indexer.addArtifactToIndex( createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
                                    context );
        packIndex( remoteRepo, context );

        //
        TrackingFetcher fetcher;
        IndexUpdateRequest updateRequest;

        // initial index download (expected: no index download)
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getNewTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );

        // corrupt local cache
        IOUtil.copy( "corrupted", new FileOutputStream( new File( localCacheDir, "nexus-maven-repository-index.gz" ) ) );

        // try download again (it would have failed if force did not update local cache)
        removeTempContext();
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getNewTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updateRequest.setForceFullUpdate( true );
        updater.fetchAndUpdateIndex( updateRequest );
    }

    public void testInitialForcedFullDownload()
        throws Exception
    {
        indexer.addArtifactToIndex( createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
                                    context );
        packIndex( remoteRepo, context );

        //
        TrackingFetcher fetcher;
        IndexUpdateRequest updateRequest;

        // initial forced full index download (expected: successfull download)
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getNewTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updateRequest.setForceFullUpdate( true );
        updater.fetchAndUpdateIndex( updateRequest );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.gz" ).exists() );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.properties" ).exists() );
    }

    public void testFailedIndexDownload()
        throws Exception
    {
        indexer.addArtifactToIndex( createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
                                    context );
        packIndex( remoteRepo, context );

        //
        TrackingFetcher fetcher;
        IndexUpdateRequest updateRequest;

        // failed download
        fetcher = new TrackingFetcher( remoteRepo )
        {
            public InputStream retrieve( String name )
                throws IOException, java.io.FileNotFoundException
            {
                if ( name.equals( IndexingContext.INDEX_FILE + ".gz" )
                    || name.equals( IndexingContext.INDEX_FILE + ".zip" ) )
                {
                    throw new IOException();
                }
                return super.retrieve( name );
            };
        };
        updateRequest = new IndexUpdateRequest( getNewTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        try
        {
            updater.fetchAndUpdateIndex( updateRequest );
            fail();
        }
        catch ( IOException e )
        {
            // expected
        }

        // try successful download
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getNewTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.gz" ).exists() );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.properties" ).exists() );
    }

    public void testCleanCacheDirectory()
        throws Exception
    {
        indexer.addArtifactToIndex( createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
                                    context );
        packIndex( remoteRepo, context );

        //
        TrackingFetcher fetcher;
        IndexUpdateRequest updateRequest;

        // initial index download (expected: successfull download)
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getNewTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );

        // new remote index delta
        indexer.addArtifactToIndex( createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.3", null ),
                                    context );
        packIndex( remoteRepo, context );

        // delta index download (expected: successfull download)
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getNewTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );

        // sanity check
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.1.gz" ).canRead() );

        // .lock files are expected to be preserved
        File lockFile = new File( localCacheDir, Locker.LOCK_FILE );
        IOUtil.copy( "", new FileOutputStream( lockFile ) );
        assertTrue( lockFile.canRead() );

        // all unknown files and directories are expected to be removed
        File unknownFile = new File( localCacheDir, "unknownFile" );
        IOUtil.copy( "", new FileOutputStream( unknownFile ) );
        File unknownDirectory = new File( localCacheDir, "unknownDirectory" );
        unknownDirectory.mkdirs();
        assertTrue( unknownFile.canRead() );
        assertTrue( unknownDirectory.isDirectory() );

        // forced full update
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getNewTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updateRequest.setForceFullUpdate( true );
        updater.fetchAndUpdateIndex( updateRequest );

        assertTrue( lockFile.canRead() );
        assertFalse( new File( localCacheDir, "nexus-maven-repository-index.1.gz" ).canRead() );
        assertFalse( unknownFile.canRead() );
        assertFalse( unknownDirectory.isDirectory() );
    }

    public void testOffline()
        throws Exception
    {
        indexer.addArtifactToIndex( createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
                                    context );
        packIndex( remoteRepo, context );

        //
        TrackingFetcher fetcher;
        IndexUpdateRequest updateRequest;

        // initial index download (expected: successfull download)
        fetcher = new TrackingFetcher( remoteRepo );
        IndexingContext testContext = getNewTempContext();
        updateRequest = new IndexUpdateRequest( testContext );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );

        // recreate local index from the cache without remote access (and NULL fetcher)
        // fetcher is null, so we no way to assert that
        updateRequest = new IndexUpdateRequest( testContext );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setOffline( true );
        updateRequest.setResourceFetcher( null );
        updater.fetchAndUpdateIndex( updateRequest );
        assertGroupCount( 1, "commons-lang", testContext );

        // recreate local index from the cache without remote access (and NOT NULL fetcher)
        updateRequest = new IndexUpdateRequest( testContext );
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( testContext );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updateRequest.setOffline( true );
        updater.fetchAndUpdateIndex( updateRequest );
        assertEquals( 0, fetcher.getRetrievedResources().size() );
        assertGroupCount( 1, "commons-lang", testContext );
    }

}
