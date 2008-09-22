package org.sonatype.nexus.util;

import java.io.File;
import java.net.URL;

import org.codehaus.plexus.PlexusTestCase;

public class FileUtilsTest
    extends PlexusTestCase
{
    public void testUNCPath()
        throws Exception
    {
        if ( FileUtils.isWindows() )
        {
            String filepath = "\\\\someserver\blah\blah\blah.jar";
            assertTrue( FileUtils.validFileUrl( filepath ) );
            
            File file = new File( filepath );
            assertTrue( FileUtils.validFile( file ) );
            
            String badFilepath = "someserver\blah\blah\blah.jar";
            assertFalse( FileUtils.validFileUrl( badFilepath ) );
            
            String urlFilepath = "file:////someserver/blah/blah.jar";
            assertTrue( FileUtils.validFileUrl( filepath ) );
            
            assertTrue( FileUtils.validFile( new File( new URL( urlFilepath ).getFile() ) ) );
        }
        else
        {
            assertTrue( true );
        }
    }
}
