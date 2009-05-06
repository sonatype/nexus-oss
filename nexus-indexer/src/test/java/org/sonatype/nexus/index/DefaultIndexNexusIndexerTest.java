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
import java.util.Map;
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
import org.sonatype.nexus.index.search.grouping.GAGrouping;
import org.sonatype.nexus.index.search.grouping.GGrouping;
import org.sonatype.nexus.index.updater.DefaultIndexUpdater;

/** @author Jason van Zyl */
public class DefaultIndexNexusIndexerTest
    extends AbstractRepoNexusIndexerTest
{

    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context = nexusIndexer.addIndexingContext(
            "test-default",
            "test",
            repo,
            indexDir,
            null,
            null,
            DEFAULT_CREATORS );

        assertNull( context.getTimestamp() ); // unknown upon creation

        nexusIndexer.scan( context );

        assertNotNull( context.getTimestamp() );
    }

    public void testSearchGroupedClasses()
        throws Exception
    {
        {
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "com/thoughtworks/qdox" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GAGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );
            Map<String, ArtifactInfoGroup> r = response.getResults(); 
    
            assertEquals( r.toString(), 2, r.size() ); // qdox and testng
    
            assertTrue( r.containsKey( "qdox : qdox" ) );
            assertTrue( r.containsKey( "org.testng : testng" ) );
            assertEquals( "qdox : qdox", r.get( "qdox : qdox" ).getGroupKey() );
            assertEquals( "org.testng : testng", r.get( "org.testng : testng" ).getGroupKey() );
        }

        {
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "com.thoughtworks.qdox" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GAGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );
            Map<String, ArtifactInfoGroup> r = response.getResults(); 
            assertEquals( r.toString(), 2, r.size() );
    
            assertTrue( r.containsKey( "qdox : qdox" ) );
            assertTrue( r.containsKey( "org.testng : testng" ) );
            assertEquals( "qdox : qdox", r.get( "qdox : qdox" ).getGroupKey() );
            assertEquals( "org.testng : testng", r.get( "org.testng : testng" ).getGroupKey() );
        }

        {
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "thoughtworks" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GAGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );
            Map<String, ArtifactInfoGroup> r = response.getResults(); 
            assertEquals( r.toString(), 2, r.size() );
            assertTrue( r.containsKey( "qdox : qdox" ) );
            assertTrue( r.containsKey( "org.testng : testng" ) );
            assertEquals( "qdox : qdox", r.get( "qdox : qdox" ).getGroupKey() );
            assertEquals( "org.testng : testng", r.get( "org.testng : testng" ).getGroupKey() );
        }
        
        {
            // an implicit class name wildcard
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "Logger" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );

            Map<String, ArtifactInfoGroup> r = response.getResults(); 
            assertEquals( r.toString(), 2, r.size() );

            Iterator<ArtifactInfoGroup> it = r.values().iterator();
            
            ArtifactInfoGroup ig1 = it.next();
            assertEquals( r.toString(), "org.slf4j", ig1.getGroupKey() );

            ArtifactInfoGroup ig2 = it.next();
            assertEquals( r.toString(), "org.testng", ig2.getGroupKey() );
        }

        {
            // a lower case search
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "logger" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );
            Map<String, ArtifactInfoGroup> r = response.getResults(); 
            assertEquals( r.toString(), 2, r.size() );

            Iterator<ArtifactInfoGroup> it = r.values().iterator();
            
            ArtifactInfoGroup ig1 = it.next();
            assertEquals( r.toString(), "org.slf4j", ig1.getGroupKey() );
            
            ArtifactInfoGroup ig2 = it.next();
            assertEquals( r.toString(), "org.testng", ig2.getGroupKey() );
        }
        
        {
            // explicit class name wildcard without terminator
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "*.Logger" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );
            Map<String, ArtifactInfoGroup> r = response.getResults(); 
            assertEquals( r.toString(), 2, r.size() );
            Iterator<ArtifactInfoGroup> it = r.values().iterator();
            ArtifactInfoGroup ig1 = it.next();
            assertEquals( r.toString(), "org.slf4j", ig1.getGroupKey() );
            ArtifactInfoGroup ig2 = it.next();
            assertEquals( r.toString(), "org.testng", ig2.getGroupKey() );
        }
        
        {
            // explicit class name wildcard with terminator
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "*.Logger " );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );
            Map<String, ArtifactInfoGroup> r = response.getResults(); 
            assertEquals( r.toString(), 2, r.size() );
            Iterator<ArtifactInfoGroup> it = r.values().iterator();
            ArtifactInfoGroup ig1 = it.next();
            assertEquals( r.toString(), "org.slf4j", ig1.getGroupKey() );
            ArtifactInfoGroup ig2 = it.next();
            assertEquals( r.toString(), "org.testng", ig2.getGroupKey() );
        }
        
        {
            // a class name wildcard
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "*Logger" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );
            Map<String, ArtifactInfoGroup> r = response.getResults(); 
            assertEquals( r.toString(), 3, r.size() );
            
            Iterator<ArtifactInfoGroup> it = r.values().iterator();
            
            ArtifactInfoGroup ig1 = it.next();
            assertEquals( r.toString(), "commons-logging", ig1.getGroupKey() );  // Jdk14Logger and LogKitLogger
            
            ArtifactInfoGroup ig2 = it.next();
            assertEquals( r.toString(), "org.slf4j", ig2.getGroupKey() );
            
            ArtifactInfoGroup ig3 = it.next();
            assertEquals( r.toString(), "org.testng", ig3.getGroupKey() );
        }

        {
            // exact class name
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "org/apache/commons/logging/LogConfigurationException" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GAGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );
            
            Map<String, ArtifactInfoGroup> r = response.getResults();
            assertEquals( r.toString(), 2, r.size() );  // jcl104-over-slf4j and commons-logging
        }
        
        {
            // implicit class name pattern
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "org.apache.commons.logging.LogConfigurationException" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GAGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );
            
            Map<String, ArtifactInfoGroup> r = response.getResults();
            assertEquals( r.toString(), 2, r.size() );  // jcl104-over-slf4j and commons-logging
        }
        
        {
            // exact class name
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "^org.apache.commons.logging.LogConfigurationException$" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GAGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );
            
            Map<String, ArtifactInfoGroup> r = response.getResults();
            assertEquals( r.toString(), 2, r.size() );  // jcl104-over-slf4j and commons-logging
        }
        
        {
            // package name prefix
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "^org.apache.commons.logging" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GAGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );
            
            Map<String, ArtifactInfoGroup> r = response.getResults();
            assertEquals( r.toString(), 2, r.size() );  // jcl104-over-slf4j and commons-logging
        }
        
        {
            Query q = nexusIndexer.constructQuery( ArtifactInfo.NAMES, "*slf4j*Logg*" );
            GroupedSearchRequest request = new GroupedSearchRequest( q, new GAGrouping() );
            GroupedSearchResponse response = nexusIndexer.searchGrouped( request );
            
            Map<String, ArtifactInfoGroup> r = response.getResults();
            assertEquals( r.toString(), 2, r.size() );
            
            {
                ArtifactInfoGroup ig = r.values().iterator().next();
                ArrayList<ArtifactInfo> list1 = new ArrayList<ArtifactInfo>( ig.getArtifactInfos() );
                assertEquals( r.toString(), 2, list1.size() );
                
                ArtifactInfo ai1 = list1.get( 0 );
                assertEquals( "org.slf4j", ai1.groupId );
                assertEquals( "slf4j-api", ai1.artifactId );
                assertEquals( "1.4.2", ai1.version );
                ArtifactInfo ai2 = list1.get( 1 );
                assertEquals( "org.slf4j", ai2.groupId );
                assertEquals( "slf4j-api", ai2.artifactId );
                assertEquals( "1.4.1", ai2.version );
            }
            
            {
                // This was error, since slf4j-log4j12 DOES NOT HAVE any class for this search!
                ArtifactInfoGroup ig = r.get( "org.slf4j : slf4j-log4j12" );
                ArrayList<ArtifactInfo> list = new ArrayList<ArtifactInfo>( ig.getArtifactInfos() );
                assertEquals( list.toString(), 1, list.size() );
                
                ArtifactInfo ai = list.get( 0 );
                assertEquals( "org.slf4j", ai.groupId );
                assertEquals( "slf4j-log4j12", ai.artifactId );
                assertEquals( "1.4.1", ai.version );
            }
        }
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

        DefaultIndexUpdater.unpackIndexArchive(
            new ByteArrayInputStream( os.toByteArray() ),
            newIndexDir,
            context );

        IndexingContext newContext = nexusIndexer.addIndexingContext(
            "test-new",
            "test",
            null,
            newIndexDir,
            null,
            null,
            DEFAULT_CREATORS );

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

        newContext = nexusIndexer.addIndexingContext(
            "test-new",
            "test",
            null,
            newIndexDir,
            null,
            null,
            DEFAULT_CREATORS );

        assertEquals( timestamp, newContext.getTimestamp() );

        newContext.close( true );

        assertFalse( new File( newIndex, "timestamp" ).exists() );
    }

    public void testArchetype() throws Exception 
    {
        String term = "proptest";
    
        Query bq = new PrefixQuery(new Term(ArtifactInfo.GROUP_ID, term));
        TermQuery tq = new TermQuery(new Term(ArtifactInfo.PACKAGING,
            "maven-archetype"));
        Query query = new FilteredQuery(tq, new QueryWrapperFilter(bq));
    
        FlatSearchResponse response = nexusIndexer
            .searchFlat(new FlatSearchRequest(query));
    
        Collection<ArtifactInfo> r = response.getResults();
    
        assertEquals(r.toString(), 1, r.size());
    }

    public void testArchetypePackaging() throws Exception 
    {
        Query query = new TermQuery( new Term( ArtifactInfo.PACKAGING, "maven-archetype" ) );
        FlatSearchResponse response = nexusIndexer.searchFlat(new FlatSearchRequest(query));
        assertEquals(response.getResults().toString(), 4, response.getTotalHits());
    }
    
    public void testBrokenJar() throws Exception 
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

    public void testMissingPom() throws Exception 
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
        assertNotNull( ai.classNames );
    }
    
}
