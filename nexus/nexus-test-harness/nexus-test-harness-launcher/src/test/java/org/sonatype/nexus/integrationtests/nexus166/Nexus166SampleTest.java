package org.sonatype.nexus.integrationtests.nexus166;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;


/**
 * A sample test and a good starting point: <a href='https://docs.sonatype.com/display/NX/Nexus+Test-Harness'>Nexus Test-Harness</a>
 */
public class Nexus166SampleTest extends AbstractNexusIntegrationTest
{

    @Test
    public void sampleTest() throws IOException
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
