/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.ReturnValueAction;
import org.jmock.lib.action.VoidAction;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.index.AbstractIndexCreatorHelper;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * @author Eugene Kuleshov
 */
public class DefaultIndexUpdaterTest
    extends AbstractIndexCreatorHelper
{
    private File testBasedir ;

    String repositoryId = "test";

    String repositoryUrl = "http://repo1.maven.org/maven2/";

    NexusIndexer indexer;

    IndexUpdater updater;

    IndexingContext context;

    SimpleDateFormat df = new SimpleDateFormat( "yyyyMMddHHmmss.SSS Z" );

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        testBasedir = new File( getBasedir() , "/target/indexUpdater" );
        testBasedir.mkdirs();

        indexer = lookup( NexusIndexer.class );

        updater = lookup( IndexUpdater.class );

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

    public void testReplaceIndex()
        throws Exception
    {
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
            context );

        Query q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

        FlatSearchResponse response1 = indexer.searchFlat( new FlatSearchRequest( q ) );
        Collection<ArtifactInfo> content1 = response1.getResults();

        assertEquals( content1.toString(), 1, content1.size() );

        // updated index

        Directory tempIndexDirectory = new RAMDirectory();

        IndexingContext tempContext = indexer.addIndexingContext(
            repositoryId + "temp",
            repositoryId,
            null,
            tempIndexDirectory,
            repositoryUrl,
            null,
            MIN_CREATORS );

        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.3", null ),
            tempContext );

        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.4", null ),
            tempContext );

        FlatSearchResponse response2 = indexer.searchFlat( new FlatSearchRequest( q, tempContext ) );
        Collection<ArtifactInfo> tempContent = response2.getResults();
        assertEquals( tempContent.toString(), 2, tempContent.size() );

        // RAMDirectory is closed with context, forcing timestamp update
        IndexUtils.updateTimestamp( tempContext.getIndexDirectory(), tempContext.getTimestamp() );

        RAMDirectory tempDir2 = new RAMDirectory( tempContext.getIndexDirectory() );

        Date newIndexTimestamp = tempContext.getTimestamp();

        indexer.removeIndexingContext( tempContext, false );

        context.replace( tempDir2 );

        assertEquals( newIndexTimestamp, context.getTimestamp() );

        FlatSearchResponse response3 = indexer.searchFlat( new FlatSearchRequest( q ) );
        Collection<ArtifactInfo> content2 = response3.getResults();
        assertEquals( content2.toString(), 2, content2.size() );
    }

    public void testMergeIndex()
        throws Exception
    {
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
            context );

        Query q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

        {
            FlatSearchResponse response1 = indexer.searchFlat( new FlatSearchRequest( q ) );
            Collection<ArtifactInfo> content1 = response1.getResults();

            assertEquals( content1.toString(), 1, content1.size() );
        }

        // updated index

        {
            Directory tempIndexDirectory = new RAMDirectory();

            IndexingContext tempContext = indexer.addIndexingContext(
                repositoryId + "temp",
                repositoryId,
                null,
                tempIndexDirectory,
                repositoryUrl,
                null,
                MIN_CREATORS );

            // indexer.addArtifactToIndex(
            // createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
            // tempContext );

            indexer.addArtifactToIndex( createArtifactContext(
                repositoryId,
                "commons-lang",
                "commons-lang",
                "2.3",
                null ), tempContext );

            indexer.addArtifactToIndex( createArtifactContext(
                repositoryId,
                "commons-lang",
                "commons-lang",
                "2.4",
                null ), tempContext );

            FlatSearchResponse tempResponse = indexer.searchFlat( new FlatSearchRequest( q ) );
            Collection<ArtifactInfo> tempContent = tempResponse.getResults();
            assertEquals( tempContent.toString(), 3, tempContent.size() );

            RAMDirectory tempDir2 = new RAMDirectory( tempContext.getIndexDirectory() );

            indexer.removeIndexingContext( tempContext, false );

            context.merge( tempDir2 );

            FlatSearchResponse response2 = indexer.searchFlat( new FlatSearchRequest( q ) );
            Collection<ArtifactInfo> content2 = response2.getResults();
            assertEquals( content2.toString(), 3, content2.size() );
        }
    }

    public void testMergeIndexDeletes()
        throws Exception
    {
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
            context );

        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.3", null ),
            context );

        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.4", null ),
            context );

        {
            Directory tempIndexDirectory = new RAMDirectory();

            IndexingContext tempContext = indexer.addIndexingContext(
                repositoryId + "temp",
                repositoryId,
                null,
                tempIndexDirectory,
                repositoryUrl,
                null,
                MIN_CREATORS );

            indexer.addArtifactToIndex( createArtifactContext(
                repositoryId,
                "commons-lang",
                "commons-lang",
                "2.4",
                null ), tempContext );

            indexer.addArtifactToIndex( createArtifactContext(
                repositoryId,
                "commons-lang",
                "commons-lang",
                "2.2",
                null ), tempContext );

            indexer.deleteArtifactFromIndex( createArtifactContext(
                repositoryId,
                "commons-lang",
                "commons-lang",
                "2.2",
                null ), tempContext );

            indexer.deleteArtifactFromIndex( createArtifactContext(
                repositoryId,
                "commons-lang",
                "commons-lang",
                "2.4",
                null ), tempContext );

            RAMDirectory tempDir2 = new RAMDirectory( tempContext.getIndexDirectory() );

            indexer.removeIndexingContext( tempContext, false );

            context.merge( tempDir2 );
        }

        Query q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

        FlatSearchResponse response = indexer.searchFlat( new FlatSearchRequest( q ) );
        Collection<ArtifactInfo> content2 = response.getResults();

        assertEquals( content2.toString(), 1, content2.size() );
    }

    public void testMergeSearch()
        throws Exception
    {
        File repo1 = new File( getBasedir(), "src/test/nexus-658" );
        Directory indexDir1 = new RAMDirectory();

        IndexingContext context1 = indexer.addIndexingContext(
            "nexus-658",
            "nexus-658",
            repo1,
            indexDir1,
            null,
            null,
            DEFAULT_CREATORS );
        indexer.scan( context1 );

        File repo2 = new File( getBasedir(), "src/test/nexus-13" );
        Directory indexDir2 = new RAMDirectory();

        IndexingContext context2 = indexer.addIndexingContext(
            "nexus-13",
            "nexus-13",
            repo2,
            indexDir2,
            null,
            null,
            DEFAULT_CREATORS );
        indexer.scan( context2 );

        context1.merge( indexDir2 );

        Query q = new TermQuery( new Term( ArtifactInfo.SHA1, "b5e9d009320d11b9859c15d3ad3603b455fa1c85" ) );
        FlatSearchRequest request = new FlatSearchRequest( q, context1 );
        FlatSearchResponse response = indexer.searchFlat( request );

        Set<ArtifactInfo> results = response.getResults();
        ArtifactInfo artifactInfo = results.iterator().next();
        assertEquals( artifactInfo.artifactId, "dma.integration.tests" );
    }

    public void testMergeGroups()
        throws Exception
    {
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
            context );

        indexer.addArtifactToIndex( createArtifactContext(
            repositoryId,
            "commons-collections",
            "commons-collections",
            "1.0",
            null ), context );

        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "org.slf4j", "slf4j-api", "1.4.2", null ),
            context );

        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "org.slf4j", "slf4j-log4j12", "1.4.2", null ),
            context );

        {
            Directory tempIndexDirectory = new RAMDirectory();

            IndexingContext tempContext = indexer.addIndexingContext(
                repositoryId + "temp",
                repositoryId,
                null,
                tempIndexDirectory,
                repositoryUrl,
                null,
                MIN_CREATORS );

            indexer.addArtifactToIndex( createArtifactContext(
                repositoryId,
                "commons-lang",
                "commons-lang",
                "2.4",
                null ), tempContext );

            indexer.addArtifactToIndex(
                createArtifactContext( repositoryId, "junit", "junit", "3.8", null ),
                tempContext );

            indexer.addArtifactToIndex( createArtifactContext(
                repositoryId,
                "org.slf4j.foo",
                "jcl104-over-slf4j",
                "1.4.2",
                null ), context );

            RAMDirectory tempDir2 = new RAMDirectory( tempContext.getIndexDirectory() );

            indexer.removeIndexingContext( tempContext, false );

            context.merge( tempDir2 );
        }

        Set<String> rootGroups = context.getRootGroups();

        assertEquals( rootGroups.toString(), 4, rootGroups.size() );

        Set<String> allGroups = context.getAllGroups();

        assertEquals( allGroups.toString(), 5, allGroups.size() );
    }

    public void testNoIndexUpdate() throws Exception
    {
        Mockery mockery = new Mockery();

        final String indexUrl = repositoryUrl + ".index";
        final Date contextTimestamp = df.parse( "20081125010000.000 -0600" );

        final ResourceFetcher mockFetcher = mockery.mock( ResourceFetcher.class );

        final IndexingContext tempContext = mockery.mock( IndexingContext.class );

        mockery.checking(new Expectations()
        {{
            allowing( tempContext ).getIndexDirectoryFile();
            will( new ReturnValueAction (
                testBasedir ) );

            allowing( tempContext ).getTimestamp();
            will( returnValue( contextTimestamp ) );

            allowing( tempContext ).getId();
            will( returnValue( repositoryId ) );

            allowing( tempContext ).getIndexUpdateUrl();
            will( returnValue( indexUrl ) );

            allowing( tempContext ).getIndexCreators();
            will( returnValue( DEFAULT_CREATORS ) );

            oneOf( mockFetcher ).connect( repositoryId, indexUrl );

            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".properties" ), //
                with( any( File.class ) ) );

            will(new PropertiesAction()
            {
                @Override
                Properties getProperties()
                {
                    Properties properties = new Properties();
                    properties.setProperty( IndexingContext.INDEX_ID, "central" );
                    properties.setProperty( IndexingContext.INDEX_TIMESTAMP, "20081125010000.000 -0600" );
                    return properties;
                }
            });

            allowing( tempContext ).getIndexDirectoryFile();

            oneOf( mockFetcher ).disconnect();
        }});

        // tempContext.updateTimestamp( true, contextTimestamp );

        IndexUpdateRequest updateRequest = new IndexUpdateRequest( tempContext );

        updateRequest.setResourceFetcher( mockFetcher );

        updater.fetchAndUpdateIndex( updateRequest );

        mockery.assertIsSatisfied();
    }

    public void testFullIndexUpdate() throws Exception
    {
        Mockery mockery = new Mockery();

        final String indexUrl = repositoryUrl + ".index";
        final Date contextTimestamp = df.parse( "20081125010000.000 -0600" );

        final ResourceFetcher mockFetcher = mockery.mock( ResourceFetcher.class );

        final IndexingContext tempContext = mockery.mock( IndexingContext.class );

        mockery.checking(new Expectations()
        {{
            allowing( tempContext ).getIndexDirectoryFile();
            will( new ReturnValueAction (
                testBasedir ) );

            allowing( tempContext ).getTimestamp();
            will( returnValue( contextTimestamp ) );

            allowing( tempContext ).getId();
            will( returnValue( repositoryId ) );

            allowing( tempContext ).getIndexUpdateUrl();
            will( returnValue( indexUrl ) );

            allowing( tempContext ).getIndexCreators();
            will( returnValue( DEFAULT_CREATORS ) );

            oneOf( mockFetcher ).connect( repositoryId, indexUrl );

            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".properties" ), //
                with( any( File.class ) ) );
            will(new PropertiesAction()
            {
                @Override
                Properties getProperties()
                {
                    Properties properties = new Properties();
                    properties.setProperty( IndexingContext.INDEX_ID, "central" );
                    properties.setProperty( IndexingContext.INDEX_TIMESTAMP, "20081126010000.000 -0600" );
                    return properties;
                }
            });

            allowing( tempContext ).getIndexDirectoryFile();

            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".gz" ), //
                with( Expectations.<File>anything() ) );

            oneOf( tempContext ).replace( with( any( Directory.class ) ) );

            oneOf( mockFetcher ).disconnect();
        }});

        // tempContext.updateTimestamp( true, contextTimestamp );

        IndexUpdateRequest updateRequest = new IndexUpdateRequest( tempContext );

        updateRequest.setResourceFetcher( mockFetcher );

        updater.fetchAndUpdateIndex( updateRequest );

        mockery.assertIsSatisfied();
    }

    public void testIncrementalIndexUpdate() throws Exception
    {
        Mockery mockery = new Mockery();

        final String indexUrl = repositoryUrl + ".index";
        final Date contextTimestamp = df.parse( "20081128000000.000 -0600" );

        final ResourceFetcher mockFetcher = mockery.mock( ResourceFetcher.class );

        final IndexingContext tempContext = mockery.mock( IndexingContext.class );

        final Properties localProps = new Properties();
        localProps.setProperty( IndexingContext.INDEX_CHUNK_COUNTER, "1" );
        localProps.setProperty( IndexingContext.INDEX_CHAIN_ID, "someid" );

        mockery.checking(new Expectations()
        {{
            allowing( tempContext ).getTimestamp();
            will( returnValue( contextTimestamp ) );

            allowing( tempContext ).getId();
            will( returnValue( repositoryId ) );

            allowing( tempContext ).getIndexUpdateUrl();
            will( returnValue( indexUrl ) );

            allowing( tempContext ).getIndexCreators();
            will( returnValue( DEFAULT_CREATORS ) );

            oneOf( mockFetcher ).connect( repositoryId, indexUrl );

            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".properties" ), //
                with( any( File.class ) ) );

            will(new PropertiesAction()
            {
                @Override
                Properties getProperties()
                {
                    Properties properties = new Properties();
                    properties.setProperty( IndexingContext.INDEX_ID, "central" );
                    properties.setProperty( IndexingContext.INDEX_TIMESTAMP, "20081129174241.859 -0600" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_COUNTER, "3" );
                    properties.setProperty( IndexingContext.INDEX_CHAIN_ID, "someid" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_PREFIX + "0", "3" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_PREFIX + "1", "2" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_PREFIX + "2", "1" );
                    return properties;
                }
            });

            allowing( tempContext ).getIndexDirectoryFile();
            will( new IndexDirectoryFileAction (
                localProps,
                testBasedir ) );

            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".2.gz" ), //
                with( Expectations.<File>anything() ) );
            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".3.gz" ), //
                with( Expectations.<File>anything() ) );
            // could create index archive there and verify that it is merged correctly

            oneOf( tempContext ).merge( with( any( Directory.class ) ) );

            oneOf( tempContext ).merge( with( any( Directory.class ) ) );

            oneOf( mockFetcher ).disconnect();
        }});

        // tempContext.updateTimestamp( true, contextTimestamp );

        IndexUpdateRequest updateRequest = new IndexUpdateRequest( tempContext );

        updateRequest.setResourceFetcher( mockFetcher );

        updater.fetchAndUpdateIndex( updateRequest );

        mockery.assertIsSatisfied();
    }

    public void testIncrementalIndexUpdateNoCounter() throws Exception
    {
        Mockery mockery = new Mockery();

        final String indexUrl = repositoryUrl + ".index";
        final Date contextTimestamp = df.parse( "20081128000000.000 -0600" );

        final ResourceFetcher mockFetcher = mockery.mock( ResourceFetcher.class );

        final IndexingContext tempContext = mockery.mock( IndexingContext.class );

        mockery.checking(new Expectations()
        {{
            allowing( tempContext ).getIndexDirectoryFile();
            will( new ReturnValueAction(
                testBasedir ) );

            allowing( tempContext ).getTimestamp();
            will( returnValue( contextTimestamp ) );

            allowing( tempContext ).getId();
            will( returnValue( repositoryId ) );

            allowing( tempContext ).getIndexUpdateUrl();
            will( returnValue( indexUrl ) );

            allowing( tempContext ).getIndexCreators();
            will( returnValue( DEFAULT_CREATORS ) );

            oneOf( mockFetcher ).connect( repositoryId, indexUrl );

            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".properties" ), //
                with( any( File.class ) ) );

            will(new PropertiesAction()
            {
                @Override
                Properties getProperties()
                {
                    Properties properties = new Properties();
                    properties.setProperty( IndexingContext.INDEX_ID, "central" );
                    properties.setProperty( IndexingContext.INDEX_TIMESTAMP, "20081129174241.859 -0600" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_COUNTER, "3" );
                    properties.setProperty( IndexingContext.INDEX_CHAIN_ID, "someid" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_PREFIX + "0", "3" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_PREFIX + "1", "2" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_PREFIX + "2", "1" );
                    return properties;
                }
            });

            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".gz" ), //
                with( Expectations.<File>anything() ) );
            // could create index archive there and verify that it is merged correctly

            oneOf( tempContext ).replace( with( any( Directory.class ) ) );

            never( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".2.gz" ), //
                with( Expectations.<File>anything() ) );

            never( tempContext ).merge( with( any( Directory.class ) ) );

            oneOf( mockFetcher ).disconnect();
        }});

        // tempContext.updateTimestamp( true, contextTimestamp );

        IndexUpdateRequest updateRequest = new IndexUpdateRequest( tempContext );

        updateRequest.setResourceFetcher( mockFetcher );

        updater.fetchAndUpdateIndex( updateRequest );

        mockery.assertIsSatisfied();
    }

    public void testIncrementalIndexUpdateNoUpdateNecessary() throws Exception
    {
        Mockery mockery = new Mockery();

        final String indexUrl = repositoryUrl + ".index";
        final Date contextTimestamp = df.parse( "20081128000000.000 -0600" );

        final ResourceFetcher mockFetcher = mockery.mock( ResourceFetcher.class );

        final IndexingContext tempContext = mockery.mock( IndexingContext.class );

        final Properties localProps = new Properties();
        localProps.setProperty( IndexingContext.INDEX_CHUNK_COUNTER, "3" );
        localProps.setProperty( IndexingContext.INDEX_CHAIN_ID, "someid" );

        mockery.checking(new Expectations()
        {{
            allowing( tempContext ).getTimestamp();
            will( returnValue( contextTimestamp ) );

            allowing( tempContext ).getId();
            will( returnValue( repositoryId ) );

            allowing( tempContext ).getIndexUpdateUrl();
            will( returnValue( indexUrl ) );

            allowing( tempContext ).getIndexCreators();
            will( returnValue( DEFAULT_CREATORS ) );

            oneOf( mockFetcher ).connect( repositoryId, indexUrl );

            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".properties" ), //
                with( any( File.class ) ) );

            will(new PropertiesAction()
            {
                @Override
                Properties getProperties()
                {
                    Properties properties = new Properties();
                    properties.setProperty( IndexingContext.INDEX_ID, "central" );
                    properties.setProperty( IndexingContext.INDEX_TIMESTAMP, "20081129174241.859 -0600" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_COUNTER, "3" );
                    properties.setProperty( IndexingContext.INDEX_CHAIN_ID, "someid" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_PREFIX + "0", "3" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_PREFIX + "1", "2" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_PREFIX + "2", "1" );
                    return properties;
                }
            });

            allowing( tempContext ).getIndexDirectoryFile();
            will( new IndexDirectoryFileAction (
                localProps,
                testBasedir ) );

            never( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".gz" ), //
                with( Expectations.<File>anything() ) );
            // could create index archive there and verify that it is merged correctly

            never( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".1.gz" ), //
                with( Expectations.<File>anything() ) );

            never( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".2.gz" ), //
                with( Expectations.<File>anything() ) );

            never( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".3.gz" ), //
                with( Expectations.<File>anything() ) );

            never( tempContext ).merge( with( any( Directory.class ) ) );

            never( tempContext ).replace( with( any( Directory.class ) ) );

            oneOf( mockFetcher ).disconnect();
        }});

        // tempContext.updateTimestamp( true, contextTimestamp );

        IndexUpdateRequest updateRequest = new IndexUpdateRequest( tempContext );

        updateRequest.setResourceFetcher( mockFetcher );

        updater.fetchAndUpdateIndex( updateRequest );

        mockery.assertIsSatisfied();
    }

    public void testUpdateForceFullUpdate() throws Exception
    {
        Mockery mockery = new Mockery();

        final String indexUrl = repositoryUrl + ".index";
        final Date contextTimestamp = df.parse( "20081128000000.000 -0600" );

        final ResourceFetcher mockFetcher = mockery.mock( ResourceFetcher.class );

        final IndexingContext tempContext = mockery.mock( IndexingContext.class );

        mockery.checking(new Expectations()
        {{
            allowing( tempContext ).getIndexDirectoryFile();
            will( new ReturnValueAction (
                testBasedir ) );

            allowing( tempContext ).getTimestamp();
            will( returnValue( contextTimestamp ) );

            allowing( tempContext ).getId();
            will( returnValue( repositoryId ) );

            allowing( tempContext ).getIndexUpdateUrl();
            will( returnValue( indexUrl ) );

            allowing( tempContext ).getIndexCreators();
            will( returnValue( DEFAULT_CREATORS ) );

            oneOf( mockFetcher ).connect( repositoryId, indexUrl );

            never( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".properties" ), //
                with( any( File.class ) ) );

            will(new PropertiesAction()
            {
                @Override
                Properties getProperties()
                {
                    Properties properties = new Properties();
                    properties.setProperty( IndexingContext.INDEX_ID, "central" );
                    properties.setProperty( IndexingContext.INDEX_TIMESTAMP, "20081129174241.859 -0600" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_COUNTER, "3" );
                    properties.setProperty( IndexingContext.INDEX_CHAIN_ID, "someid" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_PREFIX + "0", "3" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_PREFIX + "1", "2" );
                    properties.setProperty( IndexingContext.INDEX_CHUNK_PREFIX + "2", "1" );
                    return properties;
                }
            });

            never( tempContext ).getIndexDirectoryFile();

            never( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".1.gz" ), //
                with( Expectations.<File>anything() ) );

            never( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".2.gz" ), //
                with( Expectations.<File>anything() ) );

            never( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".3.gz" ), //
                with( Expectations.<File>anything() ) );

            oneOf( mockFetcher ).retrieve(
                with( IndexingContext.INDEX_FILE + ".gz" ), //
                with( Expectations.<File>anything() ) );

            never( tempContext ).merge( with( any( Directory.class ) ) );

            never( tempContext ).merge( with( any( Directory.class ) ) );

            oneOf( tempContext ).replace( with( any( Directory.class ) ) );

            oneOf( mockFetcher ).disconnect();
        }});

        // tempContext.updateTimestamp( true, contextTimestamp );

        IndexUpdateRequest updateRequest = new IndexUpdateRequest( tempContext );

        updateRequest.setResourceFetcher( mockFetcher );

        updateRequest.setForceFullUpdate( true );

        updater.fetchAndUpdateIndex( updateRequest );

        mockery.assertIsSatisfied();
    }

    private ArtifactContext createArtifactContext( String repositoryId, String groupId, String artifactId,
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

    private String createPath( String groupId, String artifactId, String version, String classifier )
    {
        return "/" + groupId + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version
            + ( classifier == null ? "" : "-" + classifier );
    }


    abstract static class PropertiesAction extends VoidAction
    {
        @Override
        public Object invoke(Invocation invocation) throws Throwable
        {
            File file = (File) invocation.getParameter( 1 );

            Properties properties = getProperties();

            FileOutputStream is = null;
            try
            {
                is = new FileOutputStream(file);
                properties.store(is, null);
                is.flush();
            }
            finally
            {
                IOUtil.close(is);
            }

            return null;
        }

        abstract Properties getProperties();
    }

    private static class IndexDirectoryFileAction extends VoidAction
    {
        File file = null;

        public IndexDirectoryFileAction( Properties properties, File basedir )
            throws Exception
        {
            basedir.mkdirs();

            this.file = new File( basedir, IndexingContext.INDEX_FILE + ".properties" );

            FileOutputStream fos = null;
            try
            {
                fos = new FileOutputStream( this.file );

                properties.store( fos, "" );
            }
            finally
            {
                IOUtil.close( fos );
            }
        }

        @Override
        public Object invoke(Invocation invocation) throws Throwable
        {
            return this.file.getParentFile();
        }
    }
}
