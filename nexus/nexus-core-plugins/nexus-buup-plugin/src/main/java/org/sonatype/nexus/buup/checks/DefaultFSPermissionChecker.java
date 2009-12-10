package org.sonatype.nexus.buup.checks;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

@Component( role = FSPermissionChecker.class )
public class DefaultFSPermissionChecker
    implements FSPermissionChecker
{
    private static final String TEST_CONTENT = "test content, that's what this is!";

    public void checkFSPermissions( File directory )
        throws IOException
    {
        try
        {
            if ( !directory.isDirectory() )
            {
                throw new IOException( "The path \"" + directory.getAbsolutePath()
                    + "\" does not points to existing directory! " );
            }

            // try to write
            File tmpFile = new File( directory, "buup-write-test.txt" );

            FileUtils.fileWrite( tmpFile.getAbsolutePath(), TEST_CONTENT );

            // try to read
            String content = FileUtils.fileRead( tmpFile );

            if ( !StringUtils.equals( TEST_CONTENT, content ) )
            {
                throw new IOException( "Cannot read or read is incomplete of file \"" + tmpFile.getAbsolutePath()
                    + "\"!" );
            }

            // clean up
            FileUtils.forceDelete( tmpFile );
        }
        catch ( IOException e )
        {
            throw new IOException( "Nexus cannot manage directory \"" + directory.getAbsolutePath()
                + "\"! Please fix the FS permissions!" );
        }
    }
}
