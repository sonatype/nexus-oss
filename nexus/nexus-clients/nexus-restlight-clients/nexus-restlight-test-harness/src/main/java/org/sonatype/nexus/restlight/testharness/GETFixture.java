package org.sonatype.nexus.restlight.testharness;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
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

/**
 * {@link RESTTestFixture} implementation meant to capture a single HTTP GET exchange. It has the ability to capture
 * expectations about the request URI (exact or via regex pattern) and the request HTTP headers. It can also manage a
 * response {@link Document} and map of response HTTP headers for sending back to the client upon successful request
 * validation. This implementation also provides a GET-validating {@link Handler} implementation for use in the HTTP
 * {@link Server} instance, which is managed by the abstract base class.
 */
public class GETFixture
extends AbstractRESTTestFixture
{

    private Document responseDocument;

    private String uriPattern;

    private String exactURI;

    private boolean strictHeaders;

    private Map<String, Set<String>> expectedRequestHeaders;

    private Map<String, Set<String>> responseHeaders;

    public GETFixture( final String user, final String password )
    {
        super( user, password );
    }

    /**
     * Retrieve the map of HTTP headers expected to be present in the client request.
     */
    public Map<String, Set<String>> getExpectedRequestHeaders()
    {
        return expectedRequestHeaders;
    }

    /**
     * Set the map of HTTP headers expected to be present in the client request.
     */
    public void setExpectedRequestHeaders( final Map<String, Set<String>> requestHeaders )
    {
        this.expectedRequestHeaders = requestHeaders;
    }

    /**
     * Retrieve the map of HTTP headers that should be injected into the response after a client request has been
     * validated.
     */
    public Map<String, Set<String>> getResponseHeaders()
    {
        return responseHeaders;
    }

    /**
     * Set the map of HTTP headers that should be injected into the response after a client request has been validated.
     */
    public void setResponseHeaders( final Map<String, Set<String>> responseHeaders )
    {
        this.responseHeaders = responseHeaders;
    }

    /**
     * Retrieve the exact URI expectation for this fixture. If set, the client request must match it exactly (once the
     * base URL is removed, of course) or the {@link Handler} will return a 404 response code.
     */
    public String getExactURI()
    {
        return exactURI;
    }

    /**
     * Set the exact URI expectation for this fixture. If set, the client request must match it exactly (once the base
     * URL is removed, of course) or the {@link Handler} will return a 404 response code.
     */
    public void setExactURI( final String exactURI )
    {
        this.exactURI = exactURI;
    }

    /**
     * Retrieve the URI pattern (regular expression) expectation for this fixture. If set, the client request must match
     * this regex (once the base URL is removed, of course) or the {@link Handler} will return a 404 response code.
     */
    public String getURIPattern()
    {
        return uriPattern;
    }

    /**
     * Set the URI pattern (regular expression) expectation for this fixture. If set, the client request must match this
     * regex (once the base URL is removed, of course) or the {@link Handler} will return a 404 response code.
     */
    public void setURIPattern( final String uriPattern )
    {
        this.uriPattern = uriPattern;
    }

    /**
     * Retrieve the response {@link Document} instance to be used in reply to a client request handled by this fixture.
     */
    public Document getResponseDocument()
    {
        return responseDocument;
    }

    /**
     * Set the response {@link Document} instance to be used in reply to a client request handled by this fixture.
     */
    public void setResponseDocument( final Document doc )
    {
        this.responseDocument = doc;
    }

    /**
     * Return the strict flag value. If set, all request header expectations, and only those, must match exactly with
     * those specified in the client's request.
     */
    public boolean isHeaderCheckStrict()
    {
        return strictHeaders;
    }

    /**
     * Set the strict flag value. If set, all request header expectations, and only those, must match exactly with those
     * specified in the client's request.
     */
    public void setStrictHeaders( final boolean strictHeaders )
    {
        this.strictHeaders = strictHeaders;
    }

    /**
     * {@inheritDoc}
     * 
     * If the client request isn't a GET method, return a 400 HTTP response code. If the expected request headers don't
     * match those supplied by the client request, return a 400 HTTP response code. If the URI doesn't match one or more
     * of the URI expectations, or the response document is null, a 404 HTTP response code is returned. If everything
     * checks out, the HTTP response code is 200, and the response document will be serialized in pretty format to the
     * body.
     */
    public Handler getTestHandler()
    {
        Handler handler = new AbstractHandler()
        {
            public void handle( final String target, final HttpServletRequest request, final HttpServletResponse response, final int dispatch )
            throws IOException, ServletException
            {
                Logger logger = LogManager.getLogger( GETFixture.class );

                if ( !"get".equalsIgnoreCase( request.getMethod() ) )
                {
                    logger.error( "Not a GET method: " + request.getMethod() );

                    response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Wrong method: " + request.getMethod() );
                }

                if ( !checkExpectedRequestHeaders( request, isHeaderCheckStrict() ) )
                {
                    logger.error( "Wrong request headers." );

                    response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Wrong headers." );
                }

                addResponseHeaders( response );

                String uri = getExactURI();
                String matchUri = getURIPattern();
                if ( uri != null )
                {
                    if ( !request.getRequestURI().equals( uri ) )
                    {
                        logger.error( "Exact URI check is wrong.\nExpected: " + uri + "\nActual: " + request.getRequestURI() );

                        response.sendError( HttpServletResponse.SC_NOT_FOUND );
                    }
                }
                else if ( matchUri != null )
                {
                    if ( !request.getRequestURI().matches( matchUri ) )
                    {
                        logger.error( "URI pattern check is wrong.\nExpected: " + matchUri + "\nActual: " + request.getRequestURI() );

                        response.sendError( HttpServletResponse.SC_NOT_FOUND );
                    }
                }

                Document doc = getResponseDocument();
                if ( doc == null )
                {
                    logger.info( "No response document set. Returning HTTP 404 status." );

                    response.sendError( HttpServletResponse.SC_NOT_FOUND );
                }
                else
                {
                    response.setContentType( "application/xml" );
                    response.setStatus( HttpServletResponse.SC_OK );
                    new XMLOutputter( Format.getPrettyFormat() ).output( doc, response.getOutputStream() );

                    response.flushBuffer();
                }

                ( (Request) request ).setHandled( true );
            }
        };

        return handler;
    }

    /**
     * {@inheritDoc}
     */
    public GETFixture copy()
    {
        GETFixture fixture = new GETFixture( getAuthUser(), getAuthPassword() );

        fixture.setExpectedRequestHeaders( getExpectedRequestHeaders() );
        fixture.setExactURI( getExactURI() );
        fixture.setDebugEnabled( isDebugEnabled() );
        fixture.setResponseDocument( getResponseDocument() );
        fixture.setResponseHeaders( getResponseHeaders() );
        fixture.setStrictHeaders( isHeaderCheckStrict() );
        fixture.setURIPattern( getURIPattern() );

        return fixture;
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
