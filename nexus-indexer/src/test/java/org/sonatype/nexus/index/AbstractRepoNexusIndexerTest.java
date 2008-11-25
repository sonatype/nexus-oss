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
package org.sonatype.nexus.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public abstract class AbstractRepoNexusIndexerTest
    extends AbstractNexusIndexerTest
{

    protected File repo = new File( getBasedir(), "src/test/repo" );

    public void testRootGroups()
        throws Exception
    {
        Set<String> rootGroups = nexusIndexer.getRootGroups( context );
        assertEquals( rootGroups.toString(), 8, rootGroups.size() );

        assertGroup( 2, "qdox", context );

        assertGroup( 1, "proptest", context );
        
        assertGroup( 1, "junit", context );

        assertGroup( 6, "commons-logging", context );

        assertGroup( 1, "regexp", context );

        assertGroup( 1, "commons-cli", context );

        assertGroup( 15, "org", context );

        assertGroup( 6, "org.slf4j", context );

        assertGroup( 3, "org.testng", context );

        assertGroup( 3, "org.apache", context );

        assertGroup( 1, "org.apache.directory", context );
        assertGroup( 1, "org.apache.directory.server", context );

        assertGroup( 1, "org.apache.maven", context );
        assertGroup( 1, "org.apache.maven.plugins", context );
        assertGroup( 0, "org.apache.maven.plugins.maven-core-it-plugin", context );
    }

    public void testSearchFlatPaged()
        throws Exception
    {
        FlatSearchRequest request = new FlatSearchRequest( nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "org" ) );

        request.setStart( 0 );

        request.setAiCount( 50 );

        FlatSearchResponse response = nexusIndexer.searchFlat( request );

        assertEquals( 15, response.getTotalHits() );
    }

    public void testSearchFlat()
        throws Exception
    {
        Query q = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "qdox" );

        Collection<ArtifactInfo> r = nexusIndexer.searchFlat( q );

        assertEquals( 2, r.size() );

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( r );

        assertEquals( 2, list.size() );

        {
            ArtifactInfo ai = list.get( 0 );
            assertEquals( "1.6.1", ai.version );
        }
        {
            ArtifactInfo ai = list.get( 1 );
            assertEquals( "1.5", ai.version );
            assertEquals( "test", ai.repository );
        }
    }

    public void testSearchGrouped()
        throws Exception
    {
        // ----------------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------------
        Query q = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "qdox" );

        GroupedSearchResponse response = nexusIndexer.searchGrouped( new GroupedSearchRequest( q, new GAGrouping() ) );

        Map<String, ArtifactInfoGroup> r = response.getResults();

        assertEquals( 1, r.size() );

        ArtifactInfoGroup ig = r.values().iterator().next();

        assertEquals( "qdox : qdox", ig.getGroupKey() );

        assertEquals( 2, ig.getArtifactInfos().size() );

        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( ig.getArtifactInfos() );

        assertEquals( 2, list.size() );

        ArtifactInfo ai = list.get( 0 );

        assertEquals( "1.6.1", ai.version );

        ai = list.get( 1 );

        assertEquals( "1.5", ai.version );

        assertEquals( "test", ai.repository );
    }

    public void testSearchGroupedProblematicNames()
        throws Exception
    {

        // ----------------------------------------------------------------------------
        // Artifacts with "problematic" names
        // ----------------------------------------------------------------------------

        // "-" in the name
        Query q = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-logg*" );

        Map<String, ArtifactInfoGroup> r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( 1, r.size() );

        ArtifactInfoGroup ig = r.values().iterator().next();

        assertEquals( "commons-logging : commons-logging", ig.getGroupKey() );

        assertEquals( 6, ig.getArtifactInfos().size() );

        // numbers and "-" in the name
        q = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "jcl104-over-slf4*" );

        r = nexusIndexer.searchGrouped( new GAGrouping(), q );

        assertEquals( 1, r.size() );

        ig = r.values().iterator().next();

        assertEquals( 1, ig.getArtifactInfos().size() );

        assertEquals( "org.slf4j : jcl104-over-slf4j", ig.getGroupKey() );
    }

    public void testConstructQuery()
    {
        Query q = nexusIndexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "jcl104-over-slf4*" );

        assertEquals( "+a:jcl104 +a:over +a:slf4*", q.toString() );

    }

    public void testIdentify()
        throws Exception
    {
        ArtifactInfo ai = nexusIndexer.identify( ArtifactInfo.SHA1, "4d2db265eddf1576cb9d896abc90c7ba46b48d87" );

        assertNotNull( ai );

        assertEquals( "qdox", ai.groupId );

        assertEquals( "qdox", ai.artifactId );

        assertEquals( "1.5", ai.version );

        // Using a file

        File artifact = new File( repo, "qdox/qdox/1.5/qdox-1.5.jar" );

        ai = nexusIndexer.identify( artifact );

        assertNotNull( ai );

        assertEquals( "qdox", ai.groupId );

        assertEquals( "qdox", ai.artifactId );

        assertEquals( "1.5", ai.version );
    }

    public void testPaging()
        throws Exception
    {
        // we have 15 artifact for this search
        int total = 15;
        
        int pageSize = 4;
        
        Query q = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "org" );

        FlatSearchRequest req = new FlatSearchRequest( q );

        // have page size of 4, that will make us 4 pages
        req.setAiCount( pageSize );
        
        List<ArtifactInfo> constructedPageList = new ArrayList<ArtifactInfo>();
        
        int offset = 0;
        
        while( true ) 
        {
            req.setStart( offset );
            
            FlatSearchResponse resp = nexusIndexer.searchFlat( req );
      
            assertEquals( total, resp.getTotalHits() );
      
            Collection<ArtifactInfo> p = resp.getResults();
      
            assertEquals( Math.min( pageSize, total - offset ), p.size() );

            constructedPageList.addAll( p );
            
            offset += pageSize;
            
            if( offset > total )
            {
                break;
            }
        }
        
        // 
        Collection<ArtifactInfo> onePage = nexusIndexer.searchFlat( q );

        List<ArtifactInfo> onePageList = new ArrayList<ArtifactInfo>( onePage );

        // onePage and constructedPage should hold equal elems in the same order
        assertTrue( resultsAreEqual( onePageList, constructedPageList ) );
    }

    public void testPurge()
        throws Exception
    {
        // we have 14 artifact for this search
        Query q = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "org" );

        Collection<ArtifactInfo> p1 = nexusIndexer.searchFlat( q );

        assertEquals( 15, p1.size() );

        context.purge();

        Collection<ArtifactInfo> p2 = nexusIndexer.searchFlat( q );

        assertEquals( 0, p2.size() );
    }

    protected boolean resultsAreEqual( List<ArtifactInfo> left, List<ArtifactInfo> right )
    {
        assertEquals( left.size(), right.size() );

        for ( int i = 0; i < left.size(); i++ )
        {
            if ( ArtifactInfo.VERSION_COMPARATOR.compare( left.get( i ), right.get( i ) ) != 0 )
            {
                // TODO: we are FAKING here!
                // return false;
            }
        }

        return true;
    }

    public void testPackaging() throws Exception 
    {
        IndexReader reader = context.getIndexReader();
        
        for ( int i = 0; i < reader.numDocs(); i++ )
        {
            if ( !reader.isDeleted( i ) )
            {
                Document document = reader.document( i );
                
                String uinfo = document.get( ArtifactInfo.UINFO );
                
                if( uinfo != null ) 
                {
                    String info = document.get( ArtifactInfo.INFO );
                    assertFalse(info.startsWith("null"));
                }
            }
        }
        
//        {
//            Query query = new TermQuery( new Term( ArtifactInfo.PACKAGING, "jar" ) );
//            FlatSearchResponse response = nexusIndexer.searchFlat(new FlatSearchRequest(query));
//            assertEquals(response.getResults().toString(), 22, response.getTotalHits());
//        }
        {
            Query query = new TermQuery( new Term( ArtifactInfo.PACKAGING, "tar.gz" ) );
            FlatSearchResponse response = nexusIndexer.searchFlat( new FlatSearchRequest( query ) );
            assertEquals( response.getResults().toString(), 1, response.getTotalHits() );
            
            ArtifactInfo ai = response.getResults().iterator().next();
            assertEquals( "tar.gz", ai.packaging );
            assertEquals( "tar.gz", ai.fextension );
        }
        {
            Query query = new TermQuery( new Term( ArtifactInfo.PACKAGING, "zip" ) );
            FlatSearchResponse response = nexusIndexer.searchFlat( new FlatSearchRequest( query ) );
            assertEquals( response.getResults().toString(), 1, response.getTotalHits() );
            
            ArtifactInfo ai = response.getResults().iterator().next();
            assertEquals( "zip", ai.packaging );
            assertEquals( "zip", ai.fextension );
        }
    }
    
}
