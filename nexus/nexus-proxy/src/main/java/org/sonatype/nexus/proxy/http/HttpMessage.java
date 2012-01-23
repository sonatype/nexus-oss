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
package org.sonatype.nexus.proxy.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;

/**
 * Very simplistic HttpMessage abstraction. Flakey, since it does not allows (or does not parses) multiple valued
 * headers for example, and converts header names to lowerCase.
 * 
 * @author cstamas
 */
public abstract class HttpMessage
{
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private Map<String, String> headers;

    private InputStream body;

    public void readInput( InputStream is )
    {
        String line = readLine( is );

        while ( !StringUtils.isEmpty( line ) )
        {
            String name = line.substring( 0, line.indexOf( ":" ) ).trim().toLowerCase();

            String value = line.substring( line.indexOf( ":" ) + 1, line.length() ).trim();

            getHeaders().put( name, value );

            line = readLine( is );
        }

        body = is;
    }

    protected abstract String getFirstLine();

    public Map<String, String> getHeaders()
    {
        if ( headers == null )
        {
            headers = new HashMap<String, String>();
        }
        return headers;
    }

    public InputStream getBody()
    {
        return body;
    }

    public void write( OutputStream os )
        throws IOException
    {
        write( os, true );
    }

    public void write( OutputStream os, boolean flush )
        throws IOException
    {
        StringBuffer sb = new StringBuffer( getFirstLine() );

        sb.append( "\n" );

        for ( String headerName : getHeaders().keySet() )
        {
            sb.append( headerName );

            sb.append( ": " );

            sb.append( getHeaders().get( headerName ) );

            sb.append( "\n" );
        }

        sb.append( "\n" );

        os.write( sb.toString().getBytes() );

        if ( getHeaders().containsKey( "content-length" ) && getBody() != null )
        {
            final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            int n = 0;

            while ( -1 != ( n = getBody().read( buffer ) ) )
            {
                os.write( buffer, 0, n );
            }
        }

        if ( flush )
        {
            os.flush();
        }
    }

    protected String readLine( InputStream in )
    {
        // reads a line of text from an InputStream
        StringBuffer data = new StringBuffer( "" );

        int c;

        try
        {
            // if we have nothing to read, just return null
            in.mark( 1 );
            if ( in.read() == -1 )
                return null;
            else
                in.reset();

            while ( ( c = in.read() ) >= 0 )
            {
                // check for an end-of-line character
                if ( ( c == 0 ) || ( c == 10 ) || ( c == 13 ) )
                    break;
                else
                    data.append( (char) c );
            }

            // deal with the case where the end-of-line terminator is \r\n
            if ( c == 13 )
            {
                in.mark( 1 );
                if ( in.read() != 10 )
                    in.reset();
            }
        }
        catch ( Exception e )
        {
        }

        // and return what we have
        return data.toString();
    }

}
