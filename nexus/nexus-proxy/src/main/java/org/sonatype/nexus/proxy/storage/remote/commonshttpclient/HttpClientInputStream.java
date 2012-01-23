/**
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
