/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.creator.MinimalArtifactInfoIndexCreator;
import org.sonatype.nexus.index.packer.DefaultIndexPacker;
import org.sonatype.nexus.index.search.grouping.GAGrouping;
import org.sonatype.nexus.index.updater.DefaultIndexUpdater;

/** @author Jason van Zyl */
public class NexusIndexerTest
    extends AbstractIndexCreatorHelper
{

    private IndexingContext context;

    public void testQueryCreatorNG()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        Query q = null;

        // scored search against field stored in both ways (tokenized/untokenized)
        q = indexer.constructQuery( MAVEN.GROUP_ID, "commons-loggin*", SearchType.SCORED );

        // g:commons-loggin* (groupId:commons groupId:loggin*)
        assertEquals( MinimalArtifactInfoIndexCreator.FLD_GROUP_ID_KW.getKey() + ":commons-loggin* ("
            + MinimalArtifactInfoIndexCreator.FLD_GROUP_ID.getKey() + ":commons "
            + MinimalArtifactInfoIndexCreator.FLD_GROUP_ID.getKey() + ":loggin*)", q.toString() );

        // keyword search against field stored in both ways (tokenized/untokenized)
        q = indexer.constructQuery( MAVEN.GROUP_ID, "commons-logging", SearchType.KEYWORD );

        assertEquals( MinimalArtifactInfoIndexCreator.FLD_GROUP_ID_KW.getKey() + ":commons-logging", q.toString() );

        // keyword search against field having untokenized indexerField only
        q = indexer.constructQuery( MAVEN.PACKAGING, "maven-archetype", SearchType.KEYWORD );

        assertEquals( MinimalArtifactInfoIndexCreator.FLD_PACKAGING.getKey() + ":maven-archetype", q.toString() );

        // scored search against field having untokenized indexerField only
        q = indexer.constructQuery( MAVEN.PACKAGING, "maven-archetype", SearchType.SCORED );

        assertEquals( MinimalArtifactInfoIndexCreator.FLD_PACKAGING.getKey() + ":maven-archetype "
            + MinimalArtifactInfoIndexCreator.FLD_PACKAGING.getKey() + ":maven-archetype*", q.toString() );

        // scored search against field having tokenized IndexerField only (should be impossible).
        q = indexer.constructQuery( MAVEN.NAME, "Some artifact name from Pom", SearchType.SCORED );

        assertEquals( MinimalArtifactInfoIndexCreator.FLD_NAME.getKey() + ":some "
            + MinimalArtifactInfoIndexCreator.FLD_NAME.getKey() + ":artifact "
            + MinimalArtifactInfoIndexCreator.FLD_NAME.getKey() + ":name "
            + MinimalArtifactInfoIndexCreator.FLD_NAME.getKey() + ":from "
            + MinimalArtifactInfoIndexCreator.FLD_NAME.getKey() + ":pom*", q.toString() );

        // keyword search against field having tokenized IndexerField only (should be impossible).
        q = indexer.constructQuery( MAVEN.NAME, "some artifact name from Pom", SearchType.KEYWORD );

        assertNull( q );
    }

    public void testQueryCreatorNGSearch()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        String qstr = null;
        Query q = null;

        IteratorSearchRequest req = null;

        IteratorSearchResponse res = null;

        // case01: "the most usual" case:
        // explanation: commons-logging should top the results, but commons-cli will be at the end too (lower score but
        // matched "commons")
        qstr = "commons-logg";
        q = indexer.constructQuery( MAVEN.GROUP_ID, qstr, SearchType.SCORED );

        req = new IteratorSearchRequest( q );

        res = indexer.searchIterator( req );

        checkResults( MAVEN.GROUP_ID, qstr, q, res,
            getTestFile( "src/test/resources/testQueryCreatorNGSearch/case01.txt" ) );

        // case02: "the most usual" case:
        // explanation: commons-logging should top the results, but commons-cli will be at the end too (lower score but
        // matched "commons")
        qstr = "commons logg";
        q = indexer.constructQuery( MAVEN.GROUP_ID, qstr, SearchType.SCORED );

        req = new IteratorSearchRequest( q );

        res = indexer.searchIterator( req );

        checkResults( MAVEN.GROUP_ID, qstr, q, res,
            getTestFile( "src/test/resources/testQueryCreatorNGSearch/case02.txt" ) );

        // case03: "the most usual" case:
        // explanation: all "commons" matches, but commons-cli tops since it's _shorter_! (see Lucene Scoring)
        qstr = "commons";
        q = indexer.constructQuery( MAVEN.GROUP_ID, qstr, SearchType.SCORED );

        req = new IteratorSearchRequest( q );

        res = indexer.searchIterator( req );

        checkResults( MAVEN.GROUP_ID, qstr, q, res,
            getTestFile( "src/test/resources/testQueryCreatorNGSearch/case03.txt" ) );

        // case04: "the most usual" case:
        // explanation: only commons-logging matches, no commons-cli
        qstr = "log";
        q = indexer.constructQuery( MAVEN.GROUP_ID, qstr, SearchType.SCORED );

        req = new IteratorSearchRequest( q );

        res = indexer.searchIterator( req );

        checkResults( MAVEN.GROUP_ID, qstr, q, res,
            getTestFile( "src/test/resources/testQueryCreatorNGSearch/case04.txt" ) );

        // case05: "the most usual" case:
        // many matches, but at the top only the _exact_ matches for "1.0", and below all artifacts that have versions
        // that contains letters "1" and "0".
        qstr = "1.0";
        q = indexer.constructQuery( MAVEN.VERSION, "1.0", SearchType.SCORED );

        req = new IteratorSearchRequest( q );

        res = indexer.searchIterator( req );

        checkResults( MAVEN.VERSION, qstr, q, res,
            getTestFile( "src/test/resources/testQueryCreatorNGSearch/case05.txt" ) );

        // case06: "the most usual" case (for apps), "selection":
        // explanation: exactly only those artifacts, that has version "1.0"
        qstr = "1.0";
        q = indexer.constructQuery( MAVEN.VERSION, qstr, SearchType.KEYWORD );

        req = new IteratorSearchRequest( q );

        res = indexer.searchIterator( req );

        checkResults( MAVEN.VERSION, qstr, q, res,
            getTestFile( "src/test/resources/testQueryCreatorNGSearch/case06.txt" ) );

        // and comes the "trick", i will perform single _selection_!
        // I want to ensure there is an artifact present!
        // explanation: see for yourself ;)
        BooleanQuery bq = new BooleanQuery();

        Query g = indexer.constructQuery( MAVEN.GROUP_ID, "commons-logging", SearchType.KEYWORD );
        Query a = indexer.constructQuery( MAVEN.ARTIFACT_ID, "commons-logging", SearchType.KEYWORD );
        Query v = indexer.constructQuery( MAVEN.VERSION, "1.0.4", SearchType.KEYWORD );
        Query p = indexer.constructQuery( MAVEN.PACKAGING, "jar", SearchType.KEYWORD );
        Query c = indexer.constructQuery( MAVEN.CLASSIFIER, Field.NOT_PRESENT, SearchType.KEYWORD );

        // so, I am looking up GAVP (for content of those look above) that _has no_ classifier
        bq.add( g, Occur.MUST );
        bq.add( a, Occur.MUST );
        bq.add( v, Occur.MUST );
        bq.add( p, Occur.MUST );
        bq.add( c, Occur.MUST_NOT );

        // invoking the old method (was present since day 1), that will return the match only and if only there is 1 hit
        ArtifactInfo ai = indexer.identify( bq );

        // null means not "identified", so we want non-null response
        assertTrue( ai != null );

        // we assure we found what we wanted
        assertEquals( "commons-logging:commons-logging:1.0.4:null:jar", ai.toString() );
    }

    /**
     * Will "print" the result set, and suck up a file and compare the two
     */
    public void checkResults( Field field, String query, Query q, IteratorSearchResponse res, File expectedResults )
        throws IOException
    {
        // switch used for easy data collection from console (for saving new "expected" results after you assured they
        // are fine)
        boolean print = true;

        StringWriter sw = new StringWriter();

        PrintWriter pw = new PrintWriter( sw );

        String line = null;

        line =
            "### Searched for field " + field.toString() + " using query \"" + query + "\" (QC create LQL \""
                + q.toString() + "\")";

        if ( print )
        {
            System.out.println( line );
        }

        pw.println( line );

        int totalHits = 0;

        for ( ArtifactInfo ai : res )
        {
            line = ai.context + " :: " + ai.toString();

            if ( print )
            {
                System.out.println( line );
            }

            pw.println( line );

            totalHits++;
        }

        line = "### TOTAL:" + totalHits + " (response said " + res.getTotalHits() + ")";

        if ( print )
        {
            System.out.println( line );
        }

        pw.println( line );

        // compare results! Load up the reference file, but avoid line ending issues, so just read it line by line. and
        // produce it in very same fashion that previous one was
        StringWriter ressw = new StringWriter();
        PrintWriter respw = new PrintWriter( ressw );

        BufferedReader reader = new BufferedReader( new FileReader( expectedResults ) );
        String currentline = null;

        while ( ( currentline = reader.readLine() ) != null )
        {
            respw.println( currentline );
        }

        String shouldBe = ressw.toString();
        String whatWeHave = sw.toString();

        // we compare those two
        assertEquals( "Search results inconsistent!", shouldBe, whatWeHave );
    }

    public void testSearchIterator()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        Query q = indexer.constructQuery( ArtifactInfo.GROUP_ID, "qdox" );

        IteratorSearchRequest request = new IteratorSearchRequest( q );

        IteratorSearchResponse response = indexer.searchIterator( request );

        assertEquals( 2, response.getTotalHits() );

        for ( ArtifactInfo ai : response.getResults() )
        {
            assertEquals( "GroupId must match \"qdox\"!", "qdox", ai.groupId );
        }
    }

    public void testSearchIteratorWithFilter()
        throws Exception
    {
        NexusIndexer indexer = prepare();

        Query q = indexer.constructQuery( ArtifactInfo.GROUP_ID, "qdox" );

        IteratorSearchRequest request = new IteratorSearchRequest( q, new ArtifactInfoFilter()
        {
            public boolean accepts( IndexingContext ctx, ArtifactInfo ai )
            {
                // we reject version "1.5" for fun
                return !StringUtils.equals( ai.version, "1.5" );
            }
        } );

        IteratorSearchResponse response = indexer.searchIterator( request );

        assertEquals( 2, response.getTotalHits() );

        assertTrue( "Iterator has to have next (2 found, 1 filtered out)", response.getResults().hasNext() );

        ArtifactInfo ai = response.getResults().next();

        assertEquals( "1.5 is filtered out, so 1.6.1 must appear here!", "1.6.1", ai.version );
    }

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
            GroupedSearchRequest request =
                new GroupedSearchRequest( q, new GAGrouping(), String.CASE_INSENSITIVE_ORDER );
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
        assertEquals( r.toString(), 2, r.size() );
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

        Query q =
            new TermQuery( new Term( ArtifactInfo.UINFO, "org.apache.maven.plugins|maven-core-it-plugin|1.0|NA" ) );

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
        // + ai.artifactId + "/" + ai.version + "/" + ai.artifactId + "-" + ai.version;

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

        DefaultIndexPacker.packIndexArchive( context, bos );

        DefaultIndexUpdater.unpackIndexArchive( new ByteArrayInputStream( bos.toByteArray() ), newDirectory, //
            context );

        indexer.removeIndexingContext( context, false );

        indexer.addIndexingContext( indexId, //
            repositoryId, repository, newDirectory, repositoryUrl, null, indexCreators );

        WildcardQuery q = new WildcardQuery( new Term( ArtifactInfo.PACKAGING, "maven-plugin" ) );
        FlatSearchResponse response = indexer.searchFlat( new FlatSearchRequest( q ) );
        Collection<ArtifactInfo> infos = response.getResults();

        assertEquals( infos.toString(), 2, infos.size() );
    }

    private NexusIndexer prepare()
        throws Exception, IOException, UnsupportedExistingLuceneIndexException
    {
        NexusIndexer indexer = lookup( NexusIndexer.class );

        // Directory indexDir = new RAMDirectory();
        File indexDir = new File( getBasedir(), "target/index/test-" + Long.toString( System.currentTimeMillis() ) );
        FileUtils.deleteDirectory( indexDir );

        File repo = new File( getBasedir(), "src/test/repo" );

        context = indexer.addIndexingContext( "test", "test", repo, indexDir, null, null, DEFAULT_CREATORS );
        indexer.scan( context );

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

    // private void printDocs(NexusIndexer nexus) throws IOException
    // {
    // IndexingContext context = nexus.getIndexingContexts().get("test");
    // IndexReader reader = context.getIndexSearcher().getIndexReader();
    // int numDocs = reader.numDocs();
    // for (int i = 0; i < numDocs; i++) {
    // Document doc = reader.document(i);
    // System.err.println(i + " " + doc.get(ArtifactInfo.UINFO) + " : " + doc.get(ArtifactInfo.PACKAGING));
    // }
    // }
}
