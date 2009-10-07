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
 * {@link RESTTestFixture} implementation meant to capture a single HTTP POST exchange. It has the ability to capture
 * expectations about the request URI (exact or via regex pattern), the request body {@link Document}, and the request
 * HTTP headers. It can also manage a response {@link Document} and a map of response HTTP headers for sending back to
 * the client upon successful request validation. This implementation also provides a POST-validating {@link Handler}
 * implementation for use in the HTTP {@link Server} instance, which is managed by the abstract base class.
 */
public class POSTFixture
extends AbstractRESTTestFixture
{

    private Document requestDocument;
    private Document responseDocument;
    private String uriPattern;
    private String exactURI;
    private boolean strictHeaders;

    private int responseStatus = HttpServletResponse.SC_OK;

    private Map<String, Set<String>> expectedRequestHeaders;

    private Map<String, Set<String>> responseHeaders;

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
     * Retrieve the response {@link Document} instance to be used in reply to a client request handled by this fixture.
     */
    public Document getResponseDocument()
    {
        return responseDocument;
    }

    /**
     * Set the response {@link Document} instance to be used in reply to a client request handled by this fixture.
     */
    public void setResponseDocument( final Document responseDocument )
    {
        this.responseDocument = responseDocument;
    }

    /**
     * Retrieve the HTTP response code to be used in reply to a client request handled by this fixture.
     */
    public int getResponseStatus()
    {
        return responseStatus;
    }

    /**
     * Set the HTTP response code to be used in reply to a client request handled by this fixture.
     */
    public void setResponseStatus( final int responseStatus )
    {
        this.responseStatus = responseStatus;
    }

    /**
     * Retrieve the expected request {@link Document} instance to be used to validate a client request handled by this
     * fixture.
     */
    public Document getRequestDocument()
    {
        return requestDocument;
    }

    /**
     * Set the expected request {@link Document} instance to be used to validate a client request handled by this
     * fixture.
     */
    public void setRequestDocument( final Document requestDocument )
    {
        this.requestDocument = requestDocument;
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
     * Return the strict flag value. If set, all request header expectations, and only those, must match exactly with
     * those specified in the client's request.
     */
    public boolean isStrictHeaders()
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
     * If the client request isn't a POST method, return a 400 HTTP response code. If the expected request headers don't
     * match those supplied by the client request, return a 400 HTTP response code. If the request document expectation
     * is specified and the client request body doesn't match, a 400 HTTP response code (400 is used if the request body
     * cannot be parsed, too). If the URI doesn't match one or more of the URI expectations, a 404 HTTP response code is
     * returned. If everything checks out, the HTTP response code is set, and if the response document is set, it will
     * be serialized in pretty format to the body.
     */
    public Handler getTestHandler()
    {
        return new AbstractHandler()
        {
            public void handle( final String target, final HttpServletRequest request, final HttpServletResponse response, final int dispatch )
            throws IOException, ServletException
            {
                Logger logger = LogManager.getLogger( POSTFixture.class );

                if ( !"post".equalsIgnoreCase( request.getMethod() ) )
                {
                    logger.error( "Not a POST request: " + request.getMethod() );

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
                            logger.error( "Request body is wrong.\n\nExpected:\n\n" + expected + "\n\nActual:\n\n" + actual + "\n\n" );

                            response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Invalid body: doesn't match expected content." );
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

                response.setContentType( "application/xml" );
                response.setStatus( responseStatus );
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

    /**
     * {@inheritDoc}
     */
    public POSTFixture copy()
    {
        POSTFixture fixture = new POSTFixture();

        fixture.setRequestDocument( getRequestDocument() );
        fixture.setResponseStatus( getResponseStatus() );
        fixture.setExpectedRequestHeaders( getExpectedRequestHeaders() );
        fixture.setExactURI( getExactURI() );
        fixture.setDebugEnabled( isDebugEnabled() );
        fixture.setResponseDocument( getResponseDocument() );
        fixture.setResponseHeaders( getResponseHeaders() );
        fixture.setStrictHeaders( isStrictHeaders() );
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
