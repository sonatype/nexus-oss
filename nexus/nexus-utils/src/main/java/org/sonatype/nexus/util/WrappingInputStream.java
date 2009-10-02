package org.sonatype.nexus.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is a helper to do some stream wrapping easier.
 * 
 * @author cstamas
 * @since 1.4.0
 */
public abstract class WrappingInputStream
    extends InputStream
{
    private final InputStream wrappedStream;

    public WrappingInputStream( InputStream wrappedStream )
    {
        super();

        this.wrappedStream = wrappedStream;
    }
    
    protected InputStream getWrappedInputStream()
    {
        return wrappedStream;
    }

    @Override
    public int read()
        throws IOException
    {
        return getWrappedInputStream().read();
    }

    @Override
    public int read( byte b[] )
        throws IOException
    {
        return getWrappedInputStream().read( b );
    }

    @Override
    public int read( byte b[], int off, int len )
        throws IOException
    {
        return getWrappedInputStream().read( b, off, len );
    }

    @Override
    public long skip( long n )
        throws IOException
    {
        return getWrappedInputStream().skip( n );
    }

    @Override
    public int available()
        throws IOException
    {
        return getWrappedInputStream().available();
    }

    @Override
    public synchronized void mark( int readlimit )
    {
        getWrappedInputStream().mark( readlimit );
    }

    @Override
    public synchronized void reset()
        throws IOException
    {
        getWrappedInputStream().reset();
    }

    @Override
    public void close()
        throws IOException
    {
        getWrappedInputStream().close();
    }
}
