package org.sonatype.nexus.test;

import java.io.File;



public class NexusRestartTest extends AbstractNexusTest
{
    public NexusRestartTest()
    {
        super( "http://localhost:8081/nexus/content/groups/nexus-test/" );
    }
    
    public void testRestart()
    {
        
        stopNexus();
        
        try
        {
            Thread.sleep( 10000 );
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
            assert( false );
        }
        
        startNexus();
        
        try
        {
            Thread.sleep( 10000 );
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
            assert( false );
        }
        
        File artifact = downloadArtifact( "org.sonatype.nexus", "release-jar", "1", "jar", "./target/downloaded-jars" );
        
        assert( artifact.exists() );
    }
}
