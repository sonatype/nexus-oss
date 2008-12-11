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
