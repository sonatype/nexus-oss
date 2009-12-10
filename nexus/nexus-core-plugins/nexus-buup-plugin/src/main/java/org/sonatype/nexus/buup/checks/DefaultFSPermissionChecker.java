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

    public void checkFSPermissionsOnDirectory( File directory )
        throws IOException
    {
        if ( !directory.isDirectory() )
        {
            throw new IOException( "The path \"" + directory.getAbsolutePath()
                + "\" does not points to existing directory! " );
        }

        boolean read = true;
        boolean write = true;
        boolean delete = true;

        // do the tests
        File tmpFile = new File( directory, "buup-write-test.txt" );

        // try to write
        try
        {
            FileUtils.fileWrite( tmpFile.getAbsolutePath(), TEST_CONTENT );
        }
        catch ( IOException e )
        {
            write = false;
        }

        // try to read
        String content = null;
        try
        {
            content = FileUtils.fileRead( tmpFile );
        }
        catch ( IOException e )
        {
            read = false;
        }

        if ( !StringUtils.equals( TEST_CONTENT, content ) )
        {
            // throw new IOException( "Cannot read or read is incomplete of file \"" + tmpFile.getAbsolutePath()
            // + "\"!" );
            read = false;
        }

        // clean up
        try
        {
            FileUtils.forceDelete( tmpFile );
        }
        catch ( IOException e )
        {
            delete = false;
        }

        if ( !read || !write || !delete )
        {
            throw new IOException( "Nexus cannot manage directory \"" + directory.getAbsolutePath()
                + "\"! Please fix the FS permissions! (read=\"" + String.valueOf( read ) + "\", write=\""
                + String.valueOf( write ) + "\", delete=\"" + String.valueOf( delete ) + "\")" );
        }
    }
}
