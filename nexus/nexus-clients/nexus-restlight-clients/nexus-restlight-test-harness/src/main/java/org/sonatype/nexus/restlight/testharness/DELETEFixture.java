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
package org.sonatype.nexus.restlight.testharness;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DELETEFixture
    extends AbstractRESTTestFixture
{

    private Document responseDocument;

    private String uriPattern;

    private String exactURI;

    private boolean strictHeaders;

    private Map<String, Set<String>> expectedRequestHeaders;

    private Map<String, Set<String>> responseHeaders;

    public DELETEFixture( final String user, final String password )
    {
        super( user, password );
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

    public RESTTestFixture copy()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Handler getTestHandler()
    {
        Handler handler = new AbstractHandler()
        {
            public void handle( final String target, final HttpServletRequest request,
                final HttpServletResponse response, final int dispatch )
                throws IOException,
                    ServletException
            {
                Logger logger = LoggerFactory.getLogger( GETFixture.class );

                if ( !"delete".equalsIgnoreCase( request.getMethod() ) )
                {
                    logger.error( "Not a DELETE method: " + request.getMethod() );

                    response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Wrong method: " + request.getMethod() );
                }

                if ( !checkExpectedRequestHeaders( request, isStrictHeaders() ) )
                {
                    logger.error( "Wrong request headers." );

                    response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Wrong headers." );
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

                if ( getResponseDocument() != null )
                {
                    response.setContentType( "application/xml" );

                    response.setStatus( HttpServletResponse.SC_OK );

                    new XMLOutputter( Format.getPrettyFormat() ).output( getResponseDocument(), response
                        .getOutputStream() );

                    response.flushBuffer();
                }

                ( (Request) request ).setHandled( true );
            }
        };

        return handler;
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

}
