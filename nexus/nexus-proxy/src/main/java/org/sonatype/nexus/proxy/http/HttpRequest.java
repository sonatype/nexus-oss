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
package org.sonatype.nexus.proxy.http;

import java.io.InputStream;

/**
 * A very simplistic HTTP request abstraction.
 * 
 * @author cstamas
 */
public class HttpRequest
    extends HttpMessage
{
    private String method;

    private String uri;

    private String httpVersion;

    public void readInput( InputStream is )
    {
        String line = readLine( is );

        method = line.substring( 0, line.indexOf( " " ) ).toUpperCase();

        line = line.substring( line.indexOf( " " ) + 1, line.length() );

        uri = line.substring( 0, line.indexOf( " " ) );

        line = line.substring( line.indexOf( " " ) + 1, line.length() );

        httpVersion = line;

        super.readInput( is );
    }

    public String getFirstLine()
    {
        StringBuffer sb = new StringBuffer( getMethod() );

        sb.append( " " );

        sb.append( getUri() );

        sb.append( " " );

        sb.append( getHttpVersion() );

        return sb.toString();
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod( String method )
    {
        this.method = method;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri( String uri )
    {
        this.uri = uri;
    }

    public String getHttpVersion()
    {
        return httpVersion;
    }

    public void setHttpVersion( String version )
    {
        this.httpVersion = version;
    }
}
