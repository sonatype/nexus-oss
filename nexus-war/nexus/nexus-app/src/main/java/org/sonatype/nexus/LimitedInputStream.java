/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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
