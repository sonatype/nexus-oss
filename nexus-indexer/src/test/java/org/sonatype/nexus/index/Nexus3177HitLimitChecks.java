package org.sonatype.nexus.index;

import java.io.File;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.sonatype.nexus.index.context.IndexingContext;

public class Nexus3177HitLimitChecks
    extends AbstractNexusIndexerTest
{
    protected File repo = new File( getBasedir(), "src/test/repo" );
    
    protected Directory secondIndexDir = new RAMDirectory();
    
    protected IndexingContext secondContext;
    
    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {        
        context = nexusIndexer.addIndexingContext(
            "nexus-3177",
            "nexus-3177",
            repo,
            indexDir,
            null,
            null,
            DEFAULT_CREATORS );
        
        secondContext = nexusIndexer.addIndexingContext(
            "nexus-3177b",
            "nexus-3177b",
            repo,
            secondIndexDir,
            null,
            null,
            DEFAULT_CREATORS );
        
        nexusIndexer.scan( context );
        nexusIndexer.scan( secondContext );
    }
    
    @Override
    protected void unprepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        super.unprepareNexusIndexer( nexusIndexer );
        
        nexusIndexer.removeIndexingContext( secondContext, false );
    }
    
    public void testHitLimitNotReachedSingleContext()
        throws Exception
    {
        WildcardQuery q = new WildcardQuery( new Term( ArtifactInfo.UINFO, "*testng*" ) );
        
        FlatSearchRequest request = new FlatSearchRequest( q );
        request.setResultHitLimit( 5 );
        request.getContexts().add( context );
        
        FlatSearchResponse response = nexusIndexer.searchFlat( request );
        Set<ArtifactInfo> r = response.getResults();
        assertEquals( r.toString(), 4, r.size() );
        assertEquals( r.toString(), 4, response.getTotalHits() );
    }
    
    public void testHitLimitEqualSingleContext()
        throws Exception
    {
        WildcardQuery q = new WildcardQuery( new Term( ArtifactInfo.UINFO, "*testng*" ) );
        
        FlatSearchRequest request = new FlatSearchRequest( q );
        request.setResultHitLimit( 4 );
        request.getContexts().add( context );
        
        FlatSearchResponse response = nexusIndexer.searchFlat( request );
        Set<ArtifactInfo> r = response.getResults();
        assertEquals( r.toString(), 4, r.size() );
        assertEquals( r.toString(), 4, response.getTotalHits() );
    }
    
    public void testHitLimitExceededSingleContext()
        throws Exception
    {
        WildcardQuery q = new WildcardQuery( new Term( ArtifactInfo.UINFO, "*testng*" ) );
        
        FlatSearchRequest request = new FlatSearchRequest( q );
        request.setResultHitLimit( 3 );
        request.getContexts().add( context );
        
        FlatSearchResponse response = nexusIndexer.searchFlat( request );
        Set<ArtifactInfo> r = response.getResults();
        assertEquals( r.toString(), 0, r.size() );
        assertEquals( r.toString(), AbstractSearchResponse.LIMIT_EXCEEDED, response.getTotalHits() );
    }
    
    public void testHitLimitNotReachedMultipleContexts()
        throws Exception
    {
        WildcardQuery q = new WildcardQuery( new Term( ArtifactInfo.UINFO, "*testng*" ) );
        
        FlatSearchRequest request = new FlatSearchRequest( q );
        request.setResultHitLimit( 9 );
        request.setArtifactInfoComparator( ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );
        request.getContexts().add( context );
        request.getContexts().add( secondContext );
        
        FlatSearchResponse response = nexusIndexer.searchFlat( request );
        Set<ArtifactInfo> r = response.getResults();
        assertEquals( r.toString(), 8, r.size() );
        assertEquals( r.toString(), 8, response.getTotalHits() );
    }
    
    public void testHitLimitEqualMultipleContexts()
        throws Exception
    {
        WildcardQuery q = new WildcardQuery( new Term( ArtifactInfo.UINFO, "*testng*" ) );
        
        FlatSearchRequest request = new FlatSearchRequest( q );
        request.setResultHitLimit( 8 );
        request.setArtifactInfoComparator( ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );
        request.getContexts().add( context );
        request.getContexts().add( secondContext );
        
        FlatSearchResponse response = nexusIndexer.searchFlat( request );
        Set<ArtifactInfo> r = response.getResults();
        assertEquals( r.toString(), 8, r.size() );
        assertEquals( r.toString(), 8, response.getTotalHits() );
    }
    
    public void testHitLimitExceededMultipleContexts()
        throws Exception
    {
        WildcardQuery q = new WildcardQuery( new Term( ArtifactInfo.UINFO, "*testng*" ) );
        
        FlatSearchRequest request = new FlatSearchRequest( q );
        request.setResultHitLimit( 7 );
        request.setArtifactInfoComparator( ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );
        request.getContexts().add( context );
        request.getContexts().add( secondContext );
        
        FlatSearchResponse response = nexusIndexer.searchFlat( request );
        Set<ArtifactInfo> r = response.getResults();
        assertEquals( r.toString(), 0, r.size() );
        assertEquals( r.toString(), AbstractSearchResponse.LIMIT_EXCEEDED, response.getTotalHits() );
    }
}
