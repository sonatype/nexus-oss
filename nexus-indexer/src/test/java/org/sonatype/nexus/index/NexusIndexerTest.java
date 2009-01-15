/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.RAMDirectory;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.creator.IndexCreator;

/** @author Jason van Zyl */
public class NexusIndexerTest
    extends PlexusTestCase
{

    private IndexingContext context;

    public void testSearchGrouped()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        {
            Query q = indexer.constructQuery( ArtifactInfo.GROUP_ID, "qdox" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GAGrouping() );
            GroupedSearchResponse response = indexer.searchGrouped( request );
            Map<String, ArtifactInfoGroup> r = response.getResults();
            assertEquals( 1, r.size() );
        
            ArtifactInfoGroup gi0 = r.values().iterator().next();
            assertEquals( "qdox : qdox", gi0.getGroupKey() );
            List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( gi0.getArtifactInfos() );
            ArtifactInfo ai0 = list.get( 0 );
            assertEquals( "1.6.1", ai0.version );
            ArtifactInfo ai1 = list.get( 1 );
            assertEquals( "1.5", ai1.version );
            assertEquals( "test", ai1.repository );
        }
        {
            WildcardQuery q = new WildcardQuery( new Term( ArtifactInfo.UINFO, "commons-log*" ) );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GAGrouping(), String.CASE_INSENSITIVE_ORDER );
            GroupedSearchResponse response = indexer.searchGrouped( request );
            Map<String, ArtifactInfoGroup> r = response.getResults();
            assertEquals( 1, r.size() );

            ArtifactInfoGroup gi1 = r.values().iterator().next();
            assertEquals( "commons-logging : commons-logging", gi1.getGroupKey() );
        }
    }

    public void testSearchFlat()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        {
            WildcardQuery q = new WildcardQuery( new Term( ArtifactInfo.UINFO, "*testng*" ) );
            FlatSearchResponse response = indexer.searchFlat( new FlatSearchRequest( q ) );
            Set<ArtifactInfo> r = response.getResults();
            assertEquals( r.toString(), 4, r.size() );
        }

        {
            BooleanQuery bq = new BooleanQuery( true );
            bq.add( new WildcardQuery( new Term( ArtifactInfo.GROUP_ID, "testng*" ) ), Occur.SHOULD );
            bq.add( new WildcardQuery( new Term( ArtifactInfo.ARTIFACT_ID, "testng*" ) ), Occur.SHOULD );
            bq.setMinimumNumberShouldMatch( 1 );
    
            FlatSearchResponse response = indexer.searchFlat( new FlatSearchRequest( bq ) );
            Set<ArtifactInfo> r = response.getResults();
    
            assertEquals( r.toString(), 4, r.size() );
        }
    }

    public void testSearchPackaging()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        WildcardQuery q = new WildcardQuery( new Term( ArtifactInfo.PACKAGING, "maven-plugin" ) );
        FlatSearchResponse response = indexer.searchFlat( new FlatSearchRequest( q ) );
        Set<ArtifactInfo> r = response.getResults();
        assertEquals( r.toString(), 1, r.size() );
    }

    public void testIdentity()
        throws Exception
    {
        NexusIndexer nexus = prepare();

        // Search using SHA1 to find qdox 1.5

        ArtifactInfo ai = nexus.identify( ArtifactInfo.SHA1, "4d2db265eddf1576cb9d896abc90c7ba46b48d87" );

        assertNotNull( ai );

        assertEquals( "qdox", ai.groupId );

        assertEquals( "qdox", ai.artifactId );

        assertEquals( "1.5", ai.version );

        assertEquals( "test", ai.repository );

        // Using a file

        File artifact = new File( getBasedir(), "src/test/repo/qdox/qdox/1.5/qdox-1.5.jar" );

        ai = nexus.identify( artifact );

        assertNotNull( ai );

        assertEquals( "qdox", ai.groupId );

        assertEquals( "qdox", ai.artifactId );

        assertEquals( "1.5", ai.version );

        assertEquals( "test", ai.repository );
    }

    public void testUpdateArtifact()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        Query q = new TermQuery( new Term(
            ArtifactInfo.UINFO,
            "org.apache.maven.plugins|maven-core-it-plugin|1.0|NA" ) );

        FlatSearchRequest request = new FlatSearchRequest( q );

        FlatSearchResponse response1 = indexer.searchFlat( request );
        Collection<ArtifactInfo> res1 = response1.getResults(); 
        assertEquals( 1, res1.size() );

        ArtifactInfo ai = res1.iterator().next();

        assertEquals( "Maven Core Integration Test Plugin", ai.name );

        long oldSize = ai.size;

        ai.name = "bla bla bla";

        ai.size += 100;

        IndexingContext indexingContext = indexer.getIndexingContexts().get( "test" );

        // String fname = indexingContext.getRepository().getAbsolutePath() + "/" + ai.groupId.replace( '.', '/' ) + "/"
        //     + ai.artifactId + "/" + ai.version + "/" + ai.artifactId + "-" + ai.version;

        // File pom = new File( fname + ".pom" );

        // File artifact = new File( fname + ".jar" );

        indexer.addArtifactToIndex( new ArtifactContext( null, null, null, ai, null ), indexingContext );

        FlatSearchResponse response2 = indexer.searchFlat( request );
        Collection<ArtifactInfo> res2 = response2.getResults(); 
        assertEquals( 1, res2.size() );

        ArtifactInfo ai2 = res2.iterator().next();

        assertEquals( oldSize + 100, ai2.size );

        assertEquals( "bla bla bla", ai2.name );
    }

    public void testUnpack()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        String indexId = context.getId();
        String repositoryId = context.getRepositoryId();
        File repository = context.getRepository();
        String repositoryUrl = context.getRepositoryUrl();
        List<IndexCreator> indexCreators = context.getIndexCreators();
        // Directory directory = context.getIndexDirectory();

        RAMDirectory newDirectory = new RAMDirectory();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        IndexUtils.packIndexArchive( context, bos );

        IndexUtils.unpackIndexArchive( new ByteArrayInputStream( bos.toByteArray() ), newDirectory );

        indexer.removeIndexingContext( context, false );

        indexer
            .addIndexingContext( indexId, repositoryId, repository, newDirectory, repositoryUrl, null, indexCreators );

        WildcardQuery q = new WildcardQuery( new Term( ArtifactInfo.PACKAGING, "maven-plugin" ) );
        FlatSearchResponse response = indexer.searchFlat( new FlatSearchRequest( q ) );
        Collection<ArtifactInfo> infos = response.getResults(); 

        assertEquals( infos.toString(), 1, infos.size() );
    }

    private NexusIndexer prepare()
        throws Exception,
            IOException,
            UnsupportedExistingLuceneIndexException
    {
        NexusIndexer indexer = lookup( NexusIndexer.class );

        //Directory indexDir = new RAMDirectory();
        File indexDir = new File( getBasedir(), "target/index/test-" + Long.toString( System.currentTimeMillis() ) );
        FileUtils.deleteDirectory( indexDir );

        File repo = new File( getBasedir(), "src/test/repo" );

        context = indexer.addIndexingContext( "test", "test", repo, indexDir, null, null, NexusIndexer.DEFAULT_INDEX );
        indexer.scan( context );

//        IndexReader indexReader = context.getIndexSearcher().getIndexReader();
//        int numDocs = indexReader.numDocs();
//        for ( int i = 0; i < numDocs; i++ ) 
//        {
//            Document doc = indexReader.document( i );
//            System.err.println( i + " : " + doc.get( ArtifactInfo.UINFO));
//          
//        }
        return indexer;
    }

//    private void printDocs(NexusIndexer nexus) throws IOException 
//    {
//        IndexingContext context = nexus.getIndexingContexts().get("test");
//        IndexReader reader = context.getIndexSearcher().getIndexReader();
//        int numDocs = reader.numDocs();
//        for (int i = 0; i < numDocs; i++) {
//          Document doc = reader.document(i);  
//          System.err.println(i + " " + doc.get(ArtifactInfo.UINFO) + " : " + doc.get(ArtifactInfo.PACKAGING));
//        }
//    }
}
