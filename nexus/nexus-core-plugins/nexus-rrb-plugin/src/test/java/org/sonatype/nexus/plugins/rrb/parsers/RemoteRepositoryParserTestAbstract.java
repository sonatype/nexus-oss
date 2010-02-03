package org.sonatype.nexus.plugins.rrb.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

abstract class RemoteRepositoryParserTestAbstract
{

    /**
     * Extract the content of a file from the classpath.
     */
    protected String getExampleFileContent( String exampleFileName )
        throws IOException
    {
        URL exampleFileURL = this.getClass().getResource( exampleFileName );
        File file = new File( exampleFileURL.getPath() );

        StringBuilder content = new StringBuilder();
        BufferedReader input = new BufferedReader( new FileReader( file ) );
        try
        {
            String line = null;
            while ( ( line = input.readLine() ) != null )
            {
                content.append( line );
                content.append( System.getProperty( "line.separator" ) );
            }
        }
        finally
        {
            input.close();
        }

        return content.toString();
    }

}
