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

import java.io.InputStream;

/**
 * Very simplistic HTTP Response abstraction.
 * 
 * @author cstamas
 */
public class HttpResponse
    extends HttpMessage
{
    public static final int BAD_REQUEST = 400;

    public static final int FORBIDDEN = 403;

    public static final int NOT_FOUND = 404;

    public static final int INTERNAL_SERVER_ERROR = 500;

    public static final int NOT_IMPLEMENTED = 501;

    private String httpVersion;

    private int statusCode;

    private String reasonPhrase;

    public void readInput( InputStream is )
    {
        String line = readLine( is );

        httpVersion = line.substring( 0, line.indexOf( " " ) ).toUpperCase();

        line = line.substring( line.indexOf( " " ) + 1, line.length() );

        statusCode = Integer.parseInt( line.substring( 0, line.indexOf( " " ) ) );

        line = line.substring( line.indexOf( " " ) + 1, line.length() );

        reasonPhrase = line;

        super.readInput( is );
    }

    public String getFirstLine()
    {
        StringBuffer sb = new StringBuffer( getHttpVersion() );

        sb.append( " " );

        sb.append( getStatusCode() );

        sb.append( " " );

        sb.append( getReasonPhrase() );

        return sb.toString();
    }

    public String getHttpVersion()
    {
        return httpVersion;
    }

    public void setHttpVersion( String httpVersion )
    {
        this.httpVersion = httpVersion;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode( int statusCode )
    {
        this.statusCode = statusCode;
    }

    public String getReasonPhrase()
    {
        return reasonPhrase;
    }

    public void setReasonPhrase( String reasonPhrase )
    {
        this.reasonPhrase = reasonPhrase;
    }

}
