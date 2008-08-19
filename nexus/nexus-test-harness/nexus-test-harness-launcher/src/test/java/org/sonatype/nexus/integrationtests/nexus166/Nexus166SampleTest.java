package org.sonatype.nexus.integrationtests.nexus166;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;


public class Nexus166SampleTest extends AbstractNexusIntegrationTest
{

    @Test
    public void doTest() throws IOException
    {
        log.debug( "This is just an example test" );
        log.debug( "I will show you how to do a few simple things..." );
        
        File exampleFile = this.getTestFile( "example.txt" );
        
        BufferedReader reader = new BufferedReader(new FileReader(exampleFile));
        
        // we only have one line to read.
        String exampleText = reader.readLine();
        reader.close();
        
        // you get the point...
        log.debug( "exampleText: "+ exampleText );
    }
    
}
