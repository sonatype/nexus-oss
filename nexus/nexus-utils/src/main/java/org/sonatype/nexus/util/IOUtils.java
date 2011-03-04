package org.sonatype.nexus.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * Small collection of reusable utils, a-la Plexus' IOUtils.
 * 
 * @author cstamas
 */
public class IOUtils
{
    /**
     * Uses "old" IO's InputStream to read up exactly {@code count} bytes from provided input stream. Does not closes
     * the passed in stream, just consumes provided count of bytes.
     * 
     * @param count the count of bytes to read from stream
     * @param is the stream to read from
     * @return array of bytes having count length or null
     * @throws IOException
     */
    public static byte[] getBytesClassic( final int count, final InputStream is )
        throws IOException
    {
        // Create the byte array to hold the data
        byte[] bytes = new byte[count];

        // Read in all the bytes
        int offset = 0;
        int numRead = 0;
        while ( offset < bytes.length && ( numRead = is.read( bytes, offset, bytes.length - offset ) ) >= 0 )
        {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if ( offset < bytes.length )
        {
            throw new IOException( "Could not completely read file " );
        }

        return bytes;
    }

    /**
     * Use NIO's mmap to load up exactly {@code count} bytes from provided FileInputStream. Does not closes the passed
     * in stream, just consumes provided count of bytes.
     * 
     * @param count the count of bytes to read from stream
     * @param is the stream to read from
     * @return array of bytes having count length or null
     * @throws IOException
     */
    public static byte[] getBytesNioMmap( final int count, final FileInputStream fis )
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
