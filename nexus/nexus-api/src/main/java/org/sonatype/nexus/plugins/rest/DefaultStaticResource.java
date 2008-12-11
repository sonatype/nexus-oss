/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.plugins.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DefaultStaticResource
    implements StaticResource
{
    private final URL resourceURL;

    private final String path;

    private volatile URLConnection urlConnection;

    public DefaultStaticResource( URL url )
    {
        this( url, null );
    }

    public DefaultStaticResource( URL url, String path )
    {
        this.resourceURL = url;

        this.path = path;
    }

    protected synchronized boolean checkConnection()
    {
        if ( urlConnection == null )
        {
            try
            {
                urlConnection = resourceURL.openConnection();
            }
            catch ( IOException e )
            {
                // ignore it?
                urlConnection = null;
            }
        }

        return urlConnection != null;
    }

    public String getPath()
    {
        if ( path != null )
        {
            return path;
        }
        else
        {
            return resourceURL.getPath();
        }
    }

    public long getSize()
    {
        if ( checkConnection() )
        {
            return urlConnection.getContentLength();
        }
        else
        {
            return -1;
        }
    }

    public String getContentType()
    {
        if ( checkConnection() )
        {
            return urlConnection.getContentType();
        }
        else
        {
            return null;
        }
    }

    public InputStream getInputStream()
        throws IOException
    {
        if ( checkConnection() )
        {
            InputStream is = urlConnection.getInputStream();

            urlConnection = null;

            return is;
        }
        else
        {
            throw new IOException( "Invalid resource!" );
        }
    }

}
