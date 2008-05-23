package org.sonatype.nexus.test;



public class NexusRestartTest extends AbstractNexusTest
{
    public NexusRestartTest()
    {
        super( "http://localhost:8081/nexus/content/groups/nexus-test/" );
    }
    
    public void testRestart()
    {
        
        restartNexus();
        
        //File artifact = downloadArtifact( "org.sonatype.nexus", "release-jar", "1", "jar", "./target/downloaded-jars" );
        
        //assert( artifact.exists() );
    }
}
