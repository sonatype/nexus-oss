package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.proxy.access.Action;

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

    public boolean isReusable()
    {
        return wrappedLocator.isReusable();
    }

    // ==

    private class ReadLockingInputStream
        extends InputStream
    {
        private final RepositoryItemUid wrappedUid;

        private final InputStream wrappedStream;

        public ReadLockingInputStream( RepositoryItemUid wrappedUid, InputStream wrappedStream )
        {
            super();

            this.wrappedUid = wrappedUid;

            this.wrappedStream = wrappedStream;
        }

        @Override
        public int read()
            throws IOException
        {
            return wrappedStream.read();
        }

        @Override
        public int read( byte b[] )
            throws IOException
        {
            return wrappedStream.read( b );
        }

        @Override
        public int read( byte b[], int off, int len )
            throws IOException
        {
            return wrappedStream.read( b, off, len );
        }

        @Override
        public long skip( long n )
            throws IOException
        {
            return wrappedStream.skip( n );
        }

        @Override
        public int available()
            throws IOException
        {
            return wrappedStream.available();
        }

        @Override
        public synchronized void mark( int readlimit )
        {
            wrappedStream.mark( readlimit );
        }

        @Override
        public synchronized void reset()
            throws IOException
        {
            wrappedStream.reset();
        }

        @Override
        public void close()
            throws IOException
        {
            try
            {
                wrappedStream.close();
            }
            finally
            {
                wrappedUid.unlock();
            }
        }
    }
}
