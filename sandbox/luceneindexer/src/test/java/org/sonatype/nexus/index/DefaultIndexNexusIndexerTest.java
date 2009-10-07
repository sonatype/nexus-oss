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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.packer.DefaultIndexPacker;
import org.sonatype.nexus.index.updater.DefaultIndexUpdater;

public class DefaultIndexNexusIndexerTest
    extends MinimalIndexNexusIndexerTest
{
    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context =
            nexusIndexer.addIndexingContext( "test-default", "test", repo, indexDir, null, null, DEFAULT_CREATORS );

        assertNull( context.getTimestamp() ); // unknown upon creation

        nexusIndexer.scan( context );

        assertNotNull( context.getTimestamp() );
    }

    public void testPlugin()
        throws Exception
    {
        // String term = "plugin";
        // String term = "maven-core-it-plugin";
        String term = "org.apache.maven.plugins";

        // Query bq = new TermQuery(new Term(ArtifactInfo.GROUP_ID, "org.apache.maven.plugins"));
        // Query bq = new TermQuery(new Term(ArtifactInfo.ARTIFACT_ID, term));
        Query bq = new PrefixQuery( new Term( ArtifactInfo.GROUP_ID, term ) );
        // BooleanQuery bq = new BooleanQuery();
        // bq.add(new PrefixQuery(new Term(ArtifactInfo.GROUP_ID, term + "*")), Occur.SHOULD);
        // bq.add(new PrefixQuery(new Term(ArtifactInfo.ARTIFACT_ID, term + "*")), Occur.SHOULD);
        TermQuery tq = new TermQuery( new Term( ArtifactInfo.PACKAGING, "maven-plugin" ) );
        Query query = new FilteredQuery( tq, new QueryWrapperFilter( bq ) );

        FlatSearchResponse response = nexusIndexer.searchFlat( new FlatSearchRequest( query ) );

        Collection<ArtifactInfo> r = response.getResults();

        assertEquals( r.toString(), 1, r.size() );

        ArtifactInfo ai = r.iterator().next();

        assertEquals( "org.apache.maven.plugins", ai.groupId );
        assertEquals( "maven-core-it-plugin", ai.artifactId );
        assertEquals( "core-it", ai.prefix );

        List<String> goals = ai.goals;
        assertEquals( 14, goals.size() );
        assertEquals( "catch", goals.get( 0 ) );
        assertEquals( "fork", goals.get( 1 ) );
        assertEquals( "fork-goal", goals.get( 2 ) );
        assertEquals( "touch", goals.get( 3 ) );
        assertEquals( "setter-touch", goals.get( 4 ) );
        assertEquals( "generate-envar-properties", goals.get( 5 ) );
        assertEquals( "generate-properties", goals.get( 6 ) );
        assertEquals( "loadable", goals.get( 7 ) );
        assertEquals( "light-touch", goals.get( 8 ) );
        assertEquals( "package", goals.get( 9 ) );
        assertEquals( "reachable", goals.get( 10 ) );
        assertEquals( "runnable", goals.get( 11 ) );
        assertEquals( "throw", goals.get( 12 ) );
        assertEquals( "tricky-params", goals.get( 13 ) );
    }

    public void testPluginPackaging()
        throws Exception
    {
        Query query = new TermQuery( new Term( ArtifactInfo.PACKAGING, "maven-plugin" ) );
        FlatSearchResponse response = nexusIndexer.searchFlat( new FlatSearchRequest( query ) );
        // repo contains 3 artifacts with packaging "maven-plugin", but one of the is actually an archetype!
        assertEquals( response.getResults().toString(), 2, response.getTotalHits() );
    }

    public void testSearchArchetypes()
        throws Exception
    {
        // TermQuery tq = new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-archetype"));
        // BooleanQuery bq = new BooleanQuery();
        // bq.add(new WildcardQuery(new Term(ArtifactInfo.GROUP_ID, term + "*")), Occur.SHOULD);
        // bq.add(new WildcardQuery(new Term(ArtifactInfo.ARTIFACT_ID, term + "*")), Occur.SHOULD);
        // FilteredQuery query = new FilteredQuery(tq, new QueryWrapperFilter(bq));

        Query q = new TermQuery( new Term( ArtifactInfo.PACKAGING, "maven-archetype" ) );
        FlatSearchResponse response = nexusIndexer.searchFlat( new FlatSearchRequest( q ) );
        Collection<ArtifactInfo> r = response.getResults();

        assertEquals( 4, r.size() );

        Iterator<ArtifactInfo> it = r.iterator();
        {
            ArtifactInfo ai = it.next();
            assertEquals( "org.apache.directory.server", ai.groupId );
            assertEquals( "apacheds-schema-archetype", ai.artifactId );
            assertEquals( "1.0.2", ai.version );
        }
        {
            ArtifactInfo ai = it.next();
            assertEquals( "org.apache.servicemix.tooling", ai.groupId );
            assertEquals( "servicemix-service-engine", ai.artifactId );
            assertEquals( "3.1", ai.version );
        }
        {
            ArtifactInfo ai = it.next();
            assertEquals( "org.terracotta.maven.archetypes", ai.groupId );
            assertEquals( "pojo-archetype", ai.artifactId );
            assertEquals( "1.0.3", ai.version );
        }
        {
            ArtifactInfo ai = it.next();
            assertEquals( "proptest", ai.groupId );
            assertEquals( "proptest-archetype", ai.artifactId );
            assertEquals( "1.0", ai.version );
        }
    }

    public void testIndexTimestamp()
        throws Exception
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        DefaultIndexPacker.packIndexArchive( context, os );

        Thread.sleep( 1000L );

        File newIndex = new File( getBasedir(), "target/test-new" );

        Directory newIndexDir = FSDirectory.getDirectory( newIndex );

        DefaultIndexUpdater.unpackIndexArchive( new ByteArrayInputStream( os.toByteArray() ), newIndexDir, context );

        IndexingContext newContext =
            nexusIndexer.addIndexingContext( "test-new", "test", null, newIndexDir, null, null, DEFAULT_CREATORS );

        assertEquals( context.getTimestamp().getTime(), newContext.getTimestamp().getTime() );

        assertEquals( context.getTimestamp(), newContext.getTimestamp() );

        // make sure context has the same artifacts

        Query query = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "qdox" );

        FlatSearchRequest request = new FlatSearchRequest( query, newContext );
        FlatSearchResponse response = nexusIndexer.searchFlat( request );
        Collection<ArtifactInfo> r = response.getResults();

        assertEquals( 2, r.size() );

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( r );

        assertEquals( 2, list.size() );

        ArtifactInfo ai = list.get( 0 );

        assertEquals( "1.6.1", ai.version );

        ai = list.get( 1 );

        assertEquals( "1.5", ai.version );

        assertEquals( "test", ai.repository );

        Date timestamp = newContext.getTimestamp();

        newContext.close( false );

        newIndexDir = FSDirectory.getDirectory( newIndex );

        DefaultIndexUpdater.unpackIndexArchive( new ByteArrayInputStream( os.toByteArray() ), newIndexDir, context );

        newContext =
            nexusIndexer.addIndexingContext( "test-new", "test", null, newIndexDir, null, null, DEFAULT_CREATORS );

        assertEquals( timestamp, newContext.getTimestamp() );

        newContext.close( true );

        assertFalse( new File( newIndex, "timestamp" ).exists() );
    }

    public void testArchetype()
        throws Exception
    {
        String term = "proptest";

        Query bq = new PrefixQuery( new Term( ArtifactInfo.GROUP_ID, term ) );
        TermQuery tq = new TermQuery( new Term( ArtifactInfo.PACKAGING, "maven-archetype" ) );
        Query query = new FilteredQuery( tq, new QueryWrapperFilter( bq ) );

        FlatSearchResponse response = nexusIndexer.searchFlat( new FlatSearchRequest( query ) );

        Collection<ArtifactInfo> r = response.getResults();

        assertEquals( r.toString(), 1, r.size() );
    }

    public void testArchetypePackaging()
        throws Exception
    {
        Query query = new TermQuery( new Term( ArtifactInfo.PACKAGING, "maven-archetype" ) );
        FlatSearchResponse response = nexusIndexer.searchFlat( new FlatSearchRequest( query ) );
        assertEquals( response.getResults().toString(), 4, response.getTotalHits() );
    }

    public void testBrokenJar()
        throws Exception
    {
        Query q = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "brokenjar" );

        FlatSearchRequest searchRequest = new FlatSearchRequest( q );

        FlatSearchResponse response = nexusIndexer.searchFlat( searchRequest );

        Set<ArtifactInfo> r = response.getResults();

        assertEquals( r.toString(), 1, r.size() );

        ArtifactInfo ai = r.iterator().next();

        assertEquals( "brokenjar", ai.groupId );
        assertEquals( "brokenjar", ai.artifactId );
        assertEquals( "1.0", ai.version );
        assertEquals( null, ai.classNames );
    }

    public void testMissingPom()
        throws Exception
    {
        Query q = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "missingpom" );

        FlatSearchRequest searchRequest = new FlatSearchRequest( q );

        FlatSearchResponse response = nexusIndexer.searchFlat( searchRequest );

        Set<ArtifactInfo> r = response.getResults();

        assertEquals( r.toString(), 1, r.size() );

        ArtifactInfo ai = r.iterator().next();

        assertEquals( "missingpom", ai.groupId );
        assertEquals( "missingpom", ai.artifactId );
        assertEquals( "1.0", ai.version );
        // See Nexus 2318. It should be null for a jar without classes
        assertNull( ai.classNames );
    }

}
