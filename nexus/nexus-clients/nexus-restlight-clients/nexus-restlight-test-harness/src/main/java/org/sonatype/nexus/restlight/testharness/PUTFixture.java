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
package org.sonatype.nexus.restlight.testharness;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PUTFixture
    extends AbstractRESTTestFixture
{
    private Document requestDocument;

    private Document responseDocument;

    private String uriPattern;

    private String exactURI;

    private boolean strictHeaders;

    private Map<String, Set<String>> expectedRequestHeaders;

    private Map<String, Set<String>> responseHeaders;

    public PUTFixture( final String user, final String password )
    {
        super( user, password );
    }

    public Document getRequestDocument()
    {
        return requestDocument;
    }

    public void setRequestDocument( final Document requestDocument )
    {
        this.requestDocument = requestDocument;
    }

    public Document getResponseDocument()
    {
        return responseDocument;
    }

    public void setResponseDocument( final Document responseDocument )
    {
        this.responseDocument = responseDocument;
    }

    public String getUriPattern()
    {
        return uriPattern;
    }

    public void setUriPattern( final String uriPattern )
    {
        this.uriPattern = uriPattern;
    }

    public String getExactURI()
    {
        return exactURI;
    }

    public void setExactURI( final String exactURI )
    {
        this.exactURI = exactURI;
    }

    public boolean isStrictHeaders()
    {
        return strictHeaders;
    }

    public void setStrictHeaders( final boolean strictHeaders )
    {
        this.strictHeaders = strictHeaders;
    }

    public Map<String, Set<String>> getExpectedRequestHeaders()
    {
        return expectedRequestHeaders;
    }

    public void setExpectedRequestHeaders( final Map<String, Set<String>> expectedRequestHeaders )
    {
        this.expectedRequestHeaders = expectedRequestHeaders;
    }

    public Map<String, Set<String>> getResponseHeaders()
    {
        return responseHeaders;
    }

    public void setResponseHeaders( final Map<String, Set<String>> responseHeaders )
    {
        this.responseHeaders = responseHeaders;
    }

    public Handler getTestHandler()
    {
        return new AbstractHandler()
        {
            public void handle( final String target, final HttpServletRequest request,
                final HttpServletResponse response, final int dispatch )
                throws IOException,
                    ServletException
            {
                Logger logger = LogManager.getLogger( PUTFixture.class );

                if ( !"put".equalsIgnoreCase( request.getMethod() ) )
                {
                    logger.error( "Not a PUT request: " + request.getMethod() );

                    response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Wrong method: " + request.getMethod() );
                }

                if ( !checkExpectedRequestHeaders( request, isStrictHeaders() ) )
                {
                    logger.error( "Request headers are wrong." );

                    response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Wrong headers." );
                }

                if ( requestDocument != null )
                {
                    try
                    {
                        Document input = new SAXBuilder().build( request.getInputStream() );

                        XMLOutputter outputter = new XMLOutputter( Format.getCompactFormat() );
                        String expected = outputter.outputString( getRequestDocument() ).trim();
                        String actual = outputter.outputString( input ).trim();

                        if ( !expected.equals( actual ) )
                        {
                            logger.error( "Request body is wrong.\n\nExpected:\n\n" + expected + "\n\nActual:\n\n"
                                + actual + "\n\n" );

                            response.sendError(
                                HttpServletResponse.SC_BAD_REQUEST,
                                "Invalid body: doesn't match expected content." );
                        }
                    }
                    catch ( JDOMException e )
                    {
                        logger.error( "Request body cannot be parsed." );

                        response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid body: " + e.getMessage() );
                    }
                }

                addResponseHeaders( response );

                String uri = getExactURI();
                String matchUri = getUriPattern();
                if ( uri != null )
                {
                    if ( !request.getRequestURI().equals( uri ) )
                    {
                        logger.error( "Exact URI check is wrong.\nExpected: " + uri + "\nActual: "
                            + request.getRequestURI() );

                        response.sendError( HttpServletResponse.SC_NOT_FOUND );
                    }
                }
                else if ( matchUri != null )
                {
                    if ( !request.getRequestURI().matches( matchUri ) )
                    {
                        logger.error( "URI pattern check is wrong.\nExpected: " + matchUri + "\nActual: "
                            + request.getRequestURI() );

                        response.sendError( HttpServletResponse.SC_NOT_FOUND );
                    }
                }

                response.setContentType( "application/xml" );
                response.setStatus( HttpServletResponse.SC_OK );
                Document responseDoc = getResponseDocument();

                if ( responseDoc != null )
                {
                    new XMLOutputter( Format.getPrettyFormat() ).output( responseDoc, response.getOutputStream() );

                    response.flushBuffer();
                }

                ( (Request) request ).setHandled( true );
            }
        };
    }

    public RESTTestFixture copy()
    {
        PUTFixture fixture = new PUTFixture( getAuthUser(), getAuthPassword() );

        fixture.setRequestDocument( getRequestDocument() );
        fixture.setExpectedRequestHeaders( getExpectedRequestHeaders() );
        fixture.setExactURI( getExactURI() );
        fixture.setDebugEnabled( isDebugEnabled() );
        fixture.setResponseDocument( getResponseDocument() );
        fixture.setResponseHeaders( getResponseHeaders() );
        fixture.setStrictHeaders( isStrictHeaders() );
        fixture.setUriPattern( getUriPattern() );

        return fixture;
    }

    @SuppressWarnings( "unchecked" )
    protected boolean checkExpectedRequestHeaders( final HttpServletRequest request, final boolean strict )
    {
        if ( getExpectedRequestHeaders() != null )
        {
            Map<String, Set<String>> requestHeaders = new HashMap<String, Set<String>>( getExpectedRequestHeaders() );
            for ( Map.Entry<String, Set<String>> headerValues : requestHeaders.entrySet() )
            {
                Set<String> values = new HashSet<String>( headerValues.getValue() );

                Enumeration<String> detected = request.getHeaders( headerValues.getKey() );
                if ( detected != null )
                {
                    while ( detected.hasMoreElements() )
                    {
                        if ( strict && !values.remove( detected.nextElement() ) )
                        {
                            return false;
                        }
                    }

                    if ( !values.isEmpty() )
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    protected void addResponseHeaders( final HttpServletResponse response )
    {
        if ( getResponseHeaders() != null )
        {
            for ( Map.Entry<String, Set<String>> headers : getResponseHeaders().entrySet() )
            {
                String key = headers.getKey();
                for ( String value : headers.getValue() )
                {
                    response.addHeader( key, value );
                }
            }
        }
    }

}
