package org.sonatype.nexus.proxy.item;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * {@link ContentLocator} that wraps another content locator that contains GZ compressed content.
 * 
 * @author cstamas
 * @since 2.4
 */
public class GZIPContentLocator
    implements ContentLocator
{
    private final ContentLocator gzippedContent;

    private final String mimeType;

    public GZIPContentLocator( final ContentLocator gzippedContent, final String mimeType )
    {
        this.gzippedContent = checkNotNull( gzippedContent );
        this.mimeType = checkNotNull( mimeType );
    }

    @Override
    public InputStream getContent()
        throws IOException
    {
        return new GZIPInputStream( gzippedContent.getContent() );
    }

    @Override
    public String getMimeType()
    {
        return mimeType;
    }

    @Override
    public boolean isReusable()
    {
        return gzippedContent.isReusable();
    }

    public ContentLocator getGzippedContent()
    {
        return gzippedContent;
    }
}
