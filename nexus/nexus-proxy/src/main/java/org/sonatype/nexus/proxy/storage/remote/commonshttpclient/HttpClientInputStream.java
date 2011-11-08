/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.storage.remote.commonshttpclient;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpMethod;
import org.sonatype.nexus.util.WrappingInputStream;

/**
 * The Class HttpClientInputStream.
 */
public class HttpClientInputStream
    extends WrappingInputStream
{

    /**
     * The method.
     */
    private final HttpMethod method;

    /**
     * Instantiates a new http client input stream.
     *
     * @param method the method
     * @param is     the is
     */
    public HttpClientInputStream( final HttpMethod method, final InputStream is )
    {
        super( is );
        this.method = method;
    }

    @Override
    public int read()
        throws IOException
    {
        try
        {
            final int result = super.read();

            if ( result == -1 )
            {
                release();
            }

            return result;
        }
        catch ( IOException e )
        {
            release();
            throw e;
        }
    }

    @Override
    public int read( byte b[] )
        throws IOException
    {
        try
        {
            final int result = super.read( b );

            if ( result == -1 )
            {
                release();
            }

            return result;
        }
        catch ( IOException e )
        {
            release();
            throw e;
        }
    }

    @Override
    public int read( byte b[], int off, int len )
        throws IOException
    {
        try
        {
            final int result = super.read( b, off, len );

            if ( result == -1 )
            {
                release();
            }

            return result;
        }
        catch ( IOException e )
        {
            release();
            throw e;
        }
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
            release();
        }
    }

    protected void release()
    {
        method.releaseConnection();
    }

}
