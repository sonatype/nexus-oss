package org.sonatype.nexus.proxy.item;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.util.SystemPropertiesHelper;

public class ContentLocatorUtils
{
    private static final boolean USE_MMAP = SystemPropertiesHelper.getBoolean(
        "org.sonatype.nexus.proxy.item.ContentLocatorUtils.useMmap", false );

    /**
     * Reads up first bytes (count) from ContentLocator's content. It returns byte array of exact size of count, or null
     * (ie. if file is smaller).
     * 
     * @param count the count of bytes to read up (and hence, the size of byte array to be returned).
     * @param locator the ContentLocator to read from.
     * @return returns byte array of size count or null.
     * @throws IOException
     */
    public static byte[] getFirstBytes( final int count, final ContentLocator locator )
        throws IOException
    {
        if ( locator != null )
        {
            InputStream fis = null;

            try
            {
                fis = locator.getContent();

                if ( USE_MMAP && fis instanceof FileInputStream )
                {
                    return getFirstBytesNioMmap( count, (FileInputStream) fis );
                }
                else
                {
                    return getFirstBytesClassic( count, fis );
                }
            }
            finally
            {
                IOUtil.close( fis );
            }
        }

        return null;
    }

    /**
     * Uses "old" IO's InputStream to read up first bytes from provided input stream.
     * 
     * @param count the count of bytes to read from stream
     * @param is the stream to read from
     * @return array of bytes having count length or null
     * @throws IOException
     */
    public static byte[] getFirstBytesClassic( final int count, final InputStream is )
        throws IOException
    {
        final byte[] buf = new byte[count];

        if ( is.read( buf ) == count )
        {
            return buf;
        }
        else
        {
            return null;
        }
    }

    /**
     * Use NIO's mmap to load up provided FileInputStream's first bytes.
     * 
     * @param count the count of bytes to read from stream
     * @param is the stream to read from
     * @return array of bytes having count length or null
     * @throws IOException
     */
    public static byte[] getFirstBytesNioMmap( final int count, final FileInputStream fis )
        throws IOException
    {
        FileChannel fc = fis.getChannel();

        if ( fc.size() < count )
        {
            return null;
        }

        MappedByteBuffer mb = fc.map( MapMode.READ_ONLY, 0, count );

        final byte[] buf = new byte[count];

        mb.get( buf );

        return buf;
    }
}
