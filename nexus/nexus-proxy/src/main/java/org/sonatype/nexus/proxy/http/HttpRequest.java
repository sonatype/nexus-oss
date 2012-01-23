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
