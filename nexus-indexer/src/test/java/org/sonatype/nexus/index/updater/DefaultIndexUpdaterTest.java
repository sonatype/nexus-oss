/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tam�s Cserven�k (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
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
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.IOUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.VoidAction;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.IndexUtils;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * @author Eugene Kuleshov
 */
public class DefaultIndexUpdaterTest
    extends PlexusTestCase
{
    private String repositoryId = "test";

    private File repositoryDir = null;

    private String repositoryUrl = "http://repo1.maven.org/maven2/";

    private NexusIndexer indexer;

    private IndexUpdater updater;
    
    private IndexingContext context;

    private SimpleDateFormat df = new SimpleDateFormat( "yyyyMMddHHmmss.SSS Z" );

    protected void setUp()
        throws Exception
    {
        super.setUp();

        indexer = (NexusIndexer) lookup( NexusIndexer.class );

        updater = (IndexUpdater) lookup( IndexUpdater.class );
        
        Directory indexDirectory = new RAMDirectory();

        context = indexer.addIndexingContext(
            repositoryId,
            repositoryId,
            repositoryDir,
            indexDirectory,
            repositoryUrl,
            null,
            NexusIndexer.MINIMAL_INDEX );
    }

    public void testReplaceIndex()
        throws Exception
    {
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
            context );

        Query q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

        Collection<ArtifactInfo> content1 = indexer.searchFlat( q );

        assertEquals( content1.toString(), 1, content1.size() );

        // updated index

        Directory tempIndexDirectory = new RAMDirectory();

        IndexingContext tempContext = indexer.addIndexingContext(
            repositoryId + "temp",
            repositoryId,
            repositoryDir,
            tempIndexDirectory,
            repositoryUrl,
            null,
            NexusIndexer.MINIMAL_INDEX );

        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.3", null ),
            tempContext );

        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.4", null ),
            tempContext );

        q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

        Collection<ArtifactInfo> tempContent = indexer.searchFlat( q, tempContext );

        assertEquals( tempContent.toString(), 2, tempContent.size() );

        // RAMDirectory is closed with context, forcing timestamp update
        IndexUtils.updateTimestamp( tempContext.getIndexDirectory(), tempContext.getTimestamp() );

        RAMDirectory tempDir2 = new RAMDirectory( tempContext.getIndexDirectory() );

        Date newIndexTimestamp = tempContext.getTimestamp();

        indexer.removeIndexingContext( tempContext, false );

        context.replace( tempDir2 );

        assertEquals( newIndexTimestamp, context.getTimestamp() );

        q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

        Collection<ArtifactInfo> content2 = indexer.searchFlat( q );

        assertEquals( content2.toString(), 2, content2.size() );
    }

    public void testMergeIndex()
        throws Exception
    {
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
            context );

        {
            Query q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

            Collection<ArtifactInfo> content1 = indexer.searchFlat( q );

            assertEquals( content1.toString(), 1, content1.size() );
        }

        // updated index

        {
            Directory tempIndexDirectory = new RAMDirectory();

            IndexingContext tempContext = indexer.addIndexingContext(
                repositoryId + "temp",
                repositoryId,
                repositoryDir,
                tempIndexDirectory,
                repositoryUrl,
                null,
                NexusIndexer.MINIMAL_INDEX );

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

            Query q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

            Collection<ArtifactInfo> tempContent = indexer.searchFlat( q );

            assertEquals( tempContent.toString(), 3, tempContent.size() );

            RAMDirectory tempDir2 = new RAMDirectory( tempContext.getIndexDirectory() );

            indexer.removeIndexingContext( tempContext, false );

            context.merge( tempDir2 );

            q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

            Collection<ArtifactInfo> content2 = indexer.searchFlat( q );

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
                repositoryDir,
                tempIndexDirectory,
                repositoryUrl,
                null,
                NexusIndexer.MINIMAL_INDEX );

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

        Collection<ArtifactInfo> content2 = indexer.searchFlat( q );

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
            NexusIndexer.DEFAULT_INDEX );
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
            NexusIndexer.DEFAULT_INDEX );
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
                repositoryDir,
                tempIndexDirectory,
                repositoryUrl,
                null,
                NexusIndexer.MINIMAL_INDEX );

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

    public void testGetUpdateChunkName()
        throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty( "nexus.index.id", "central" );
        properties.setProperty( "nexus.index.time", "20081129174241.859 -0600" );
        properties.setProperty( "nexus.index.chunk-0", "20081129000000.000 -0600" );
        properties.setProperty( "nexus.index.chunk-1", "20081128000000.000 -0600" );
        properties.setProperty( "nexus.index.chunk-2", "20081127000000.000 -0600" );
        properties.setProperty( "nexus.index.chunk-3", "20081126000000.000 -0600" );

        {
            String updateChunkName = updater.getUpdateChunkName(df.parse("20081125010000.000 -0600"), properties);
            assertEquals(null, updateChunkName);
        }
        {
            String updateChunkName = updater.getUpdateChunkName(df.parse("20081126010000.000 -0600"), properties);
            assertEquals("nexus-maven-repository-index.20081126.zip", updateChunkName);
        }
        {
            String updateChunkName = updater.getUpdateChunkName(df.parse("20081127010000.000 -0600"), properties);
            assertEquals("nexus-maven-repository-index.20081127.zip", updateChunkName);
        }
        {
            String updateChunkName = updater.getUpdateChunkName(df.parse("20081128010000.000 -0600"), properties);
            assertEquals("nexus-maven-repository-index.20081128.zip", updateChunkName);
        }
        {
            String updateChunkName = updater.getUpdateChunkName(df.parse("20081129010000.000 -0600"), properties);
            assertEquals("nexus-maven-repository-index.20081129.zip", updateChunkName);
        }
        {
            String updateChunkName = updater.getUpdateChunkName(df.parse("20081130010000.000 -0600"), properties);
            assertEquals(null, updateChunkName);
        }
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
            allowing( tempContext ).getTimestamp();
            will( returnValue( contextTimestamp ) );
            
            allowing( tempContext ).getId();
            will( returnValue( repositoryId ) );
            
            allowing( tempContext ).getIndexUpdateUrl();
            will( returnValue( indexUrl ) );
            
            oneOf( mockFetcher ).connect( repositoryId, indexUrl );
            
            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".properties" ), //
                with( any( File.class ) ) );
            will(new PropertiesAction() 
            {
                Properties getProperties() 
                {
                    Properties properties = new Properties();
                    properties.setProperty( "nexus.index.id", "central" );
                    properties.setProperty( "nexus.index.time", "20081125010000.000 -0600" );
                    return properties;
                }
            });
            
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
            allowing( tempContext ).getTimestamp();
            will( returnValue( contextTimestamp ) );
            
            allowing( tempContext ).getId();
            will( returnValue( repositoryId ) );
            
            allowing( tempContext ).getIndexUpdateUrl();
            will( returnValue( indexUrl ) );
            
            oneOf( mockFetcher ).connect( repositoryId, indexUrl );
            
            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".properties" ), //
                with( any( File.class ) ) );
            will(new PropertiesAction() 
            {
                Properties getProperties() 
                {
                    Properties properties = new Properties();
                    properties.setProperty( "nexus.index.id", "central" );
                    properties.setProperty( "nexus.index.time", "20081126010000.000 -0600" );
                    return properties;
                }
            });
            
            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".zip" ), //
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
        
        mockery.checking(new Expectations() 
        {{
            allowing( tempContext ).getTimestamp();
            will( returnValue( contextTimestamp ) );
            
            allowing( tempContext ).getId();
            will( returnValue( repositoryId ) );
            
            allowing( tempContext ).getIndexUpdateUrl();
            will( returnValue( indexUrl ) );
            
            oneOf( mockFetcher ).connect( repositoryId, indexUrl );
            
            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".properties" ), //
                with( any( File.class ) ) );
            will(new PropertiesAction() 
            {
                Properties getProperties() 
                {
                    Properties properties = new Properties();
                    properties.setProperty( "nexus.index.id", "central" );
                    properties.setProperty( "nexus.index.time", "20081129174241.859 -0600" );
                    properties.setProperty( "nexus.index.chunk-0", "20081129000000.000 -0600" );
                    properties.setProperty( "nexus.index.chunk-1", "20081127000000.000 -0600" );
                    properties.setProperty( "nexus.index.chunk-2", "20081126000000.000 -0600" );
                    return properties;
                }
            });
            
            oneOf( mockFetcher ).retrieve( //
                with( IndexingContext.INDEX_FILE + ".20081127.zip" ), //
                with( Expectations.<File>anything() ) );
            // could create index archive there and verify that it is merged correctly
            
            oneOf( tempContext ).merge( with( any( Directory.class ) ) );
            
            oneOf( mockFetcher ).disconnect();
        }});
        
        // tempContext.updateTimestamp( true, contextTimestamp );
        
        IndexUpdateRequest updateRequest = new IndexUpdateRequest( tempContext );
        
        updateRequest.setResourceFetcher( mockFetcher );
        
        updater.fetchAndUpdateIndex( updateRequest );
        
        mockery.assertIsSatisfied();
    }
    
    private ArtifactContext createArtifactContext( String repositoryId, String groupId, String artifactId,
        String version, String classifier )
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
    
}
