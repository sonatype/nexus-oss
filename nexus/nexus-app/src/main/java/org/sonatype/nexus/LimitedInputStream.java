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
package org.sonatype.nexus;

import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream
    extends InputStream
{
    public static final long UNLIMITED = -1;

    private final InputStream is;

    private final long from;

    private final long count;

    private long readAlready = 0;

    public LimitedInputStream( InputStream is, long from, long count )
        throws IOException
    {
        super();

        this.is = is;

        this.from = from;

        this.count = count;

        is.skip( from );
    }

    public InputStream getIs()
    {
        return is;
    }

    public long getFrom()
    {
        return from;
    }

    public long getCount()
    {
        return count;
    }

    @Override
    public int read()
        throws IOException
    {
        if ( readAlready > count )
        {
            return -1;
        }

        readAlready++;

        return is.read();
    }

    public int available()
        throws IOException
    {
        return is.available();
    }

    public void close()
        throws IOException
    {
        is.close();
    }

    public synchronized void mark( int readlimit )
    {
        is.mark( readlimit );
    }

    public synchronized void reset()
        throws IOException
    {
        is.reset();
    }

    public boolean markSupported()
    {
        return is.markSupported();
    }

}
