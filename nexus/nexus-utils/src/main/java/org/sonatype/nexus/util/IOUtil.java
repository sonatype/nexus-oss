package org.sonatype.nexus.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Some utils that should end in plexus-utils.
 * 
 * @author cstamas
 */
public class IOUtil
{
    public static void copyFromStreamToFile( InputStream is, File output )
        throws IOException
    {
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream( output );

            org.codehaus.plexus.util.IOUtil.copy( is, fos );
        }
        finally
        {
            org.codehaus.plexus.util.IOUtil.close( is );

            org.codehaus.plexus.util.IOUtil.close( fos );
        }
    }

}
