/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.storage.remote.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import org.apache.http.client.methods.AbortableHttpRequest;

/**
 * Best-effort interruptable InputStream wrapper. The wrapper checks for Thread.isInterrupred before delegating to the
 * actual stream. If the thread is interrupted, the wrapper calls AbortableHttpRequest.abort() and throws
 * InterruptedIOException.
 */
class InterruptableInputStream
    extends InputStream
{

    private final InputStream stream;

    private AbortableHttpRequest request;

    public InterruptableInputStream( final AbortableHttpRequest request, final InputStream stream )
    {
        this.request = request;
        this.stream = stream;
    }

    public InterruptableInputStream( final InputStream stream )
    {
        this( null, stream );
    }

    private void abortIfInterrupted()
        throws IOException
    {
        if ( Thread.interrupted() )
        {
            if ( request != null )
            {
                request.abort();
            }
            throw new InterruptedIOException();
        }
    }

    @Override
    public int read()
        throws IOException
    {
        abortIfInterrupted();
        return stream.read();
    }

    @Override
    public int read( byte[] b )
        throws IOException
    {
        abortIfInterrupted();
        return stream.read( b );
    }

    @Override
    public int read( byte b[], int off, int len )
        throws IOException
    {
        abortIfInterrupted();
        return stream.read( b, off, len );
    }

    @Override
    public long skip( long n )
        throws IOException
    {
        abortIfInterrupted();
        return stream.skip( n );
    }

    @Override
    public int available()
        throws IOException
    {
        abortIfInterrupted();
        return stream.available();
    }

    @Override
    public void close()
        throws IOException
    {
        // do not throw InterruptedIOException here!
        // this will not close the stream and likely mask original exception!
        stream.close();
    }

    @Override
    public void mark( int readlimit )
    {
        stream.mark( readlimit );
    }

    @Override
    public void reset()
        throws IOException
    {
        abortIfInterrupted();
        stream.reset();
    }

    @Override
    public boolean markSupported()
    {
        return stream.markSupported();
    }
}
