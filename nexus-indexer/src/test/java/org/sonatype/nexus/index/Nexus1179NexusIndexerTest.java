package org.sonatype.nexus.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Query;

public class Nexus1179NexusIndexerTest
extends AbstractNexusIndexerTest
{
    protected File repo = new File( getBasedir(), "src/test/nexus-1179" );

    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context = nexusIndexer.addIndexingContext(
            "nexus-1179",
            "nexus-1179",
            repo,
            indexDir,
            null,
            null,
            DEFAULT_CREATORS );
        nexusIndexer.scan( context );
    }
    
    public void testSearchFlat()
        throws Exception
    {
        Query q = nexusIndexer.constructQuery( ArtifactInfo.GROUP_ID, "*" );
        FlatSearchResponse response = nexusIndexer.searchFlat( new FlatSearchRequest( q ) );
        Collection<ArtifactInfo> r = response.getResults(); 
    
        assertEquals( 4, r.size() );
    
        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( r );
    
        ArtifactInfo ai = null;
    
        // g a v p c #1
        ai = list.get( 0 );
    
        assertEquals( "ant", ai.groupId );
        assertEquals( "ant", ai.artifactId );
        assertEquals( "1.6.5", ai.version );
        assertEquals( "jar", ai.packaging );
        assertEquals( null, ai.classifier );
        assertEquals( "nexus-1179", ai.repository );
        assertEquals( "jar", ai.fextension );
    
        // g a v p c #2
        ai = list.get( 1 );
        
        assertEquals( "ant", ai.groupId );
        assertEquals( "ant", ai.artifactId );
        assertEquals( "1.5.1", ai.version );
        assertEquals( null, ai.packaging );
        assertEquals( null, ai.classifier );
        assertEquals( "nexus-1179", ai.repository );
        assertEquals( "pom", ai.fextension );
    
        // g a v p c #3
        ai = list.get( 2 );
    
        assertEquals( "asm", ai.groupId );
        assertEquals( "asm-commons", ai.artifactId );
        assertEquals( "3.1", ai.version );
        assertEquals( "jar", ai.packaging );
        assertEquals( null, ai.classifier );
        assertEquals( "nexus-1179", ai.repository );
        assertEquals( "pom", ai.fextension );
        
        // g a v p c #4
        ai = list.get( 3 );
    
        assertEquals( "org", ai.groupId );
        assertEquals( "test", ai.artifactId );
        assertEquals( "1.0", ai.version );
        assertEquals( null, ai.packaging );
        assertEquals( null, ai.classifier );
        assertEquals( "nexus-1179", ai.repository );
        assertEquals( "pom", ai.fextension );
    }
}
