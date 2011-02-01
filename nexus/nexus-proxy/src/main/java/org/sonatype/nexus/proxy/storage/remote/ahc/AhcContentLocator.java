package org.sonatype.nexus.proxy.storage.remote.ahc;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.proxy.item.ContentLocator;

import com.ning.http.client.AsyncHttpClient;

/**
 * ContentLocator backed by AsyncHttpClient offering multiple strategies: reusable and non-reusable. When reusable. it
 * actually performs HTTP GET on every getContent() invocation.
 * 
 * @author cstamas
 * @deprecated This class is not used anymore, but we may resurrect it, since it does introduce <em>reusable</em>
 *             ContentLocator.
 */
public class AhcContentLocator
    implements ContentLocator
{
    private final AsyncHttpClient client;

    private final String itemUrl;

    private final long length;

    private final long lastModified;

    private final String mimeType;

    private final InputStream inputStream;

    public AhcContentLocator( final AsyncHttpClient client, final String itemUrl, final long length,
                              final long lastModified, final String mimeType, final InputStream is )
    {
        this.client = client;
        this.itemUrl = itemUrl;
        this.length = length;
        this.lastModified = lastModified;
        this.mimeType = mimeType;
        this.inputStream = is;
    }

    @Override
    public InputStream getContent()
        throws IOException
    {
        if ( inputStream != null )
        {
            return inputStream;
        }
        else
        {
            return AHCUtils.fetchContent( client, itemUrl );
        }
    }

    @Override
    public String getMimeType()
    {
        return mimeType;
    }

    @Override
    public boolean isReusable()
    {
        return inputStream == null;
    }

    public String getItemUrl()
    {
        return itemUrl;
    }

    public long getLength()
    {
        return length;
    }

    public long getLastModified()
    {
        return lastModified;
    }
}
