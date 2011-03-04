package org.sonatype.nexus.proxy.item;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.util.IOUtils;
import org.sonatype.nexus.util.SystemPropertiesHelper;

public class ContentLocatorUtils
{
    private static final boolean USE_MMAP = SystemPropertiesHelper.getBoolean(
        "org.sonatype.nexus.proxy.item.ContentLocatorUtils.useMmap", false );

    /**
     * Reads up first bytes (exactly {@code count} of them) from ContentLocator's content. It returns byte array of
     * exact size of count, or null (ie. if file is smaller).
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
                    return IOUtils.getBytesNioMmap( count, (FileInputStream) fis );
                }
                else
                {
                    return IOUtils.getBytesClassic( count, fis );
                }
            }
            finally
            {
                IOUtil.close( fis );
            }
        }

        return null;
    }
}
