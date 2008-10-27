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
