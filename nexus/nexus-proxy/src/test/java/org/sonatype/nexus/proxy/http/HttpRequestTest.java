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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.codehaus.plexus.util.IOUtil;

public class HttpRequestTest
    extends TestCase
{

    public void testSimple() throws IOException
    {
        StringBuffer sb = new StringBuffer();

        sb
            .append( "GET http://repo1.maven.org/maven2/org/apache/lucene/lucene-core/2.3.1/lucene-core-2.3.1.pom HTTP/1.1" );
        sb.append( "\n" );
        sb.append( "Accept: application/json" );
        sb.append( "\n" );
        sb.append( "SomeHeader : someHeader" );
        sb.append( "\n" );
        sb.append( "OtherHeader :other" );
        sb.append( "\n" );
        sb.append( "\n" );
        sb.append( "Here comes the body" );
        sb.append( "\n" );
        sb.append( "And some more." );
        
        ByteArrayInputStream bis = new ByteArrayInputStream(sb.toString().getBytes());;
        HttpRequest req = new HttpRequest();
        req.readInput( bis );
        
        assertEquals( "GET", req.getMethod() );
        assertEquals( "http://repo1.maven.org/maven2/org/apache/lucene/lucene-core/2.3.1/lucene-core-2.3.1.pom", req.getUri() );
        assertEquals( "HTTP/1.1", req.getHttpVersion() );
        assertEquals( 3, req.getHeaders().size() );
        assertEquals( "application/json", req.getHeaders().get( "accept" ) );
        assertEquals( "someHeader", req.getHeaders().get( "someheader" ) );
        assertEquals( "other", req.getHeaders().get( "otherheader" ) );
        assertEquals( "Here comes the body\nAnd some more.", IOUtil.toString( req.getBody() ) );
    }
}
