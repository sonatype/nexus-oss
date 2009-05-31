package org.damian;

import java.util.Set;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.damian.creators.SampleIndexCreator;
import org.sonatype.nexus.index.ArtifactInfo;

public class SampleAppTest
    extends PlexusTestCase
{
    private SampleApp app;
    
    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        
        context.put( "repository.path", "src/test/resources/repo" );
        context.put( "index.path", "target/indexOutput" );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        app = lookup( SampleApp.class );
    }
    
    public void testAddIndexContext()
        throws Exception
    {
        app.index();
        
        Set<ArtifactInfo> artifacts = app.searchIndex( ArtifactInfo.ARTIFACT_ID, "*" );
        
        assertNotNull( "returned artifacts is null", artifacts );
        assertFalse( "returned artifacts is empty", artifacts.isEmpty() );
    }
    
    public void testSearch()
        throws Exception
    {
        app.index();
        
        Set<ArtifactInfo> artifacts = app.searchIndex( ArtifactInfo.ARTIFACT_ID, "hivedoc-plugin" );
        
        assertNotNull( "returned artifacts is null", artifacts );
        assertFalse( "returned artifacts is empty", artifacts.isEmpty() );
        assertEquals( "returned artifacts has more than 1 entry", 1, artifacts.size() );
        assertEquals( "returned artifact not correct item", "hivedoc-plugin", artifacts.iterator().next().artifactId );
    }
    
    public void testSampleSearch()
        throws Exception
    {
        app.index();
        
        Set<ArtifactInfo> artifacts = app.searchIndex( SampleIndexCreator.MY_FIELD, "value" );
        
        assertNotNull( "returned artifacts is null", artifacts );
        assertFalse( "returned artifacts is empty", artifacts.isEmpty() );
        
        for ( ArtifactInfo ai : artifacts )
        {
            assertEquals( "returned artifact has invalid data", "value", ai.getAttributes().get( SampleIndexCreator.MY_FIELD ) );    
        }
    }
}
