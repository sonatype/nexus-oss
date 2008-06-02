package org.sonatype.nexus.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;

import org.junit.Test;



public class NexusRestartTest extends AbstractNexusIntegrationTest
{
    public NexusRestartTest()
    {
        super( "http://localhost:8081/nexus/content/groups/nexus-test/" );
    }
    
    @Test
    public void restartNexus()
    {
        
        stopNexus();
        
        startNexus();
        
        File artifact = downloadArtifact( "org.sonatype.nexus", "release-jar", "1", "jar", "./target/downloaded-restart-jars" );
        
        assert( artifact.exists() );
        
        File outputDirectory = unpackArtifact( artifact, "./target/extracted-jars" );
        
        try
        {
            assertTrue( outputDirectory.exists() );
            
            File[] files = outputDirectory.listFiles( new FilenameFilter()
            {
                public boolean accept(File dir, String name) {
                if ("nexus-test-harness-1.txt".equals( name ))
                {
                    return true;
                }
                
                return false;
            };} );
            
            assertTrue( files.length == 1 );
            
            complete();
        }
        finally
        {
            File[] files = outputDirectory.listFiles();
            for ( int i = 0; i < files.length; i++ )
            {
                files[i].delete();
            }
            outputDirectory.delete();
        }
    }
}
