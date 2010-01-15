package org.sonatype.nexus.index.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;

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

    private IndexingContext getTempContext()
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

        // initial index download (expected: no index download)
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertEquals( 2, fetcher.getRetrievedResources().size() );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.gz" ).exists() );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.properties" ).exists() );

        // update the same index (expected: no index download)
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertEquals( 1, fetcher.getRetrievedResources().size() );
        assertEquals( "nexus-maven-repository-index.properties", fetcher.getRetrievedResources().get( 0 ) );

        // nuke index but keep the cache (expected: no index download)
        removeTempContext();
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertEquals( 1, fetcher.getRetrievedResources().size() );
        assertEquals( "nexus-maven-repository-index.properties", fetcher.getRetrievedResources().get( 0 ) );

        // incremental remote update
        indexer.addArtifactToIndex( createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.3", null ),
                                    context );
        packIndex( remoteRepo, context );

        // update via cache (expected: incremental chunk download)
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertEquals( 2, fetcher.getRetrievedResources().size() );
        assertEquals( "nexus-maven-repository-index.properties", fetcher.getRetrievedResources().get( 0 ) );
        assertEquals( "nexus-maven-repository-index.1.gz", fetcher.getRetrievedResources().get( 1 ) );

        // kill the cache, but keep the index (expected: full index download)
        FileUtils.deleteDirectory( localCacheDir );
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertEquals( 2, fetcher.getRetrievedResources().size() );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.gz" ).exists() );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.properties" ).exists() );
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
        updateRequest = new IndexUpdateRequest( getTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );

        // corrupt local cache
        IOUtil.copy( "corrupted", new FileOutputStream( new File( localCacheDir, "nexus-maven-repository-index.gz" ) ) );

        // try download again (it would have failed if force did not update local cache)
        removeTempContext();
        fetcher = new TrackingFetcher( remoteRepo );
        updateRequest = new IndexUpdateRequest( getTempContext() );
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
        updateRequest = new IndexUpdateRequest( getTempContext() );
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
        updateRequest = new IndexUpdateRequest( getTempContext() );
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
        updateRequest = new IndexUpdateRequest( getTempContext() );
        updateRequest.setLocalIndexCacheDir( localCacheDir );
        updateRequest.setResourceFetcher( fetcher );
        updater.fetchAndUpdateIndex( updateRequest );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.gz" ).exists() );
        assertTrue( new File( localCacheDir, "nexus-maven-repository-index.properties" ).exists() );
    }
}
