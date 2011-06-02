package org.sonatype.nexus.proxy.maven;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.item.StorageFileItem;

/**
 * Maven specific "static" utilities.
 * 
 * @author cstamas
 */
public class MUtils
{
    /**
     * This method will read up a stream usually created from .sha1/.md5. The method CLOSES the passed in stream!
     * 
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String readDigestFromStream( final InputStream inputStream )
        throws IOException
    {
        try
        {
            return StringUtils.chomp( IOUtil.toString( inputStream, "UTF-8" ) ).trim().split( " " )[0];
        }
        finally
        {
            IOUtil.close( inputStream );
        }
    }

    /**
     * Reads up a hash as string from StorageFileItem pointing to .sha1/.md5 files.
     * 
     * @param inputFileItem
     * @return
     * @throws IOException
     */
    public static String readDigestFromFileItem( final StorageFileItem inputFileItem )
        throws IOException
    {
        return readDigestFromStream( inputFileItem.getInputStream() );
    }
}
