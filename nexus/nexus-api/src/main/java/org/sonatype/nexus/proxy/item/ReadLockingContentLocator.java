package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.util.WrappingInputStream;

/**
 * This is a simple wrapper implementation of ContentLocator, that wraps any other ContentLocator, while doing proper
 * {@link Action} read locking against the UID the locator points to.
 * 
 * @author cstamas
 */
public class ReadLockingContentLocator
    implements ContentLocator
{
    private final RepositoryItemUid wrappedUid;

    private final ContentLocator wrappedLocator;

    public ReadLockingContentLocator( RepositoryItemUid wrappedUid, ContentLocator wrappedLocator )
    {
        this.wrappedUid = wrappedUid;

        this.wrappedLocator = wrappedLocator;
    }

    public InputStream getContent()
        throws IOException
    {
        wrappedUid.lock( Action.read );

        return new ReadLockingInputStream( wrappedUid, wrappedLocator.getContent() );
    }

    public String getMimeType()
    {
        return wrappedLocator.getMimeType();
    }

    public boolean isReusable()
    {
        return wrappedLocator.isReusable();
    }

    // ==

    private class ReadLockingInputStream
        extends WrappingInputStream
    {
        private final RepositoryItemUid wrappedUid;

        public ReadLockingInputStream( RepositoryItemUid wrappedUid, InputStream wrappedStream )
        {
            super(wrappedStream);

            this.wrappedUid = wrappedUid;
        }

        @Override
        public void close()
            throws IOException
        {
            try
            {
                super.close();
            }
            finally
            {
                wrappedUid.unlock();
            }
        }
    }
}
