/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
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
