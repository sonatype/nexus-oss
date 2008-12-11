/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
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
