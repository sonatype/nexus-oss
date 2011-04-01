package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

/**
 * An abstract wrapper for ContentLocator. It implements all methods, but is declared abstract. Subclass it to add some
 * spice.
 * 
 * @author cstamas
 */
public abstract class AbstractWrappingContentLocator
    implements ContentLocator
{
    private final ContentLocator contentLocator;

    public AbstractWrappingContentLocator( final ContentLocator contentLocator )
    {
        this.contentLocator = contentLocator;
    }

    protected ContentLocator getTarget()
    {
        return contentLocator;
    }

    @Override
    public InputStream getContent()
        throws IOException
    {
        return getTarget().getContent();
    }

    @Override
    public String getMimeType()
    {
        return getTarget().getMimeType();
    }

    @Override
    public boolean isReusable()
    {
        return getTarget().isReusable();
    }
}
