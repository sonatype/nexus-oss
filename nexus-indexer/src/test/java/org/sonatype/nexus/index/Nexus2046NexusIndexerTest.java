package org.sonatype.nexus.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Query;

public class Nexus2046NexusIndexerTest
extends AbstractNexusIndexerTest
{
    protected File repo = new File( getBasedir(), "src/test/nexus-2046" );

    @Override
    protected void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        context = nexusIndexer.addIndexingContext(
            "nexus-2046",
            "nexus-2046",
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
    
        assertEquals( 1, r.size() );
    
        List<ArtifactInfo> list = new ArrayList<ArtifactInfo>( r );
    
        ArtifactInfo ai = null;
    
        // g a v p c #1
        ai = list.get( 0 );
    
        assertEquals( "org.maven.ide.eclipse", ai.groupId );
        assertEquals( "org.maven.ide.eclipse.feature", ai.artifactId );
        assertEquals( "0.9.7", ai.version );
        assertEquals( "eclipse-feature", ai.packaging );
        assertEquals( null, ai.classifier );
        assertEquals( "nexus-2046", ai.repository );
        assertEquals( "jar", ai.fextension );
    }
}
