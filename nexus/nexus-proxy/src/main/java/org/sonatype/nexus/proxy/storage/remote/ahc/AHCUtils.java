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
package org.sonatype.nexus.proxy.storage.remote.ahc;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Future;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.BodyDeferringAsyncHandler;
import com.ning.http.client.BodyDeferringAsyncHandler.BodyDeferringInputStream;
import com.ning.http.client.Response;
import com.ning.http.util.DateUtil;
import com.ning.http.util.DateUtil.DateParseException;

public class AHCUtils
{
    public static BodyDeferringInputStream fetchContent( final AsyncHttpClient client, final String itemUrl )
        throws IOException
    {
        try
        {
            final PipedOutputStream po = new PipedOutputStream();

            final PipedInputStream pi = new PipedInputStream( po );

            final BodyDeferringAsyncHandler hrah = new BodyDeferringAsyncHandler( po );

            Future<Response> f = client.prepareGet( itemUrl ).execute( hrah );

            return new BodyDeferringInputStream( f, hrah, pi );
        }
        catch ( Exception e )
        {
            if ( e instanceof IOException )
            {
                throw (IOException) e;
            }
            else
            {
                throw new IOException( e );
            }
        }
    }

    public static long getContentLength( final Response response, final long defaultValue )
    {
        final String contentLengthStr = response.getHeader( "content-length" );

        if ( null != contentLengthStr )
        {
            try
            {
                return Long.parseLong( contentLengthStr );
            }
            catch ( NumberFormatException e )
            {
                // neglect
                return defaultValue;
            }
        }
        else
        {
            return defaultValue;
        }

    }

    public static long getLastModified( final Response response, final long defaultValue )
    {
        final String lastModifiedStr = response.getHeader( "last-modified" );

        if ( null != lastModifiedStr )
        {
            try
            {
                return DateUtil.parseDate( lastModifiedStr ).getTime();
            }
            catch ( DateParseException e )
            {
                // neglect
                return defaultValue;
            }
        }
        else
        {
            return defaultValue;
        }
    }

    public static boolean isAnyOfTheseStatusCodes( final Response response, int... codes )
    {
        for ( int code : codes )
        {
            if ( code == response.getStatusCode() )
            {
                return true;
            }
        }

        return false;
    }
}
