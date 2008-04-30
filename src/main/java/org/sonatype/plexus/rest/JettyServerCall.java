/**
  * Copyright (C) 2008 Sonatype Inc. 
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
package org.sonatype.plexus.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.util.Series;

import com.noelios.restlet.http.HttpServerCall;

/**
 * A wrapper for Jetty's HttpConnection that converts it to Restlet.org's HttpServerCall.
 * 
 * @author cstamas
 */
public class JettyServerCall
    extends HttpServerCall
{

    private HttpConnection httpConnection;

    public HttpConnection getHttpConnection()
    {
        return httpConnection;
    }

    public void setHttpConnection( HttpConnection httpConnection )
    {
        this.httpConnection = httpConnection;
    }

    /**
     * The constructor able to override the request URI. We are simply filling the underlying HttpServlerCall with
     * proper values got from httpConnection.
     */
    @SuppressWarnings( "unchecked" )
    public JettyServerCall( HttpConnection httpConnection, String requestUri )
    {
        super(
            Logger.getLogger( JettyServerCall.class.getName() ),
            httpConnection.getRequest().getLocalAddr(),
            httpConnection.getRequest().getLocalPort() );

        this.httpConnection = httpConnection;

        Request req = httpConnection.getRequest();

        setClientAddress( req.getRemoteAddr() );
        setClientPort( req.getRemotePort() );
        setConfidential( req.isSecure() );
        setHostDomain( req.getLocalName() );
        setHostPort( req.getLocalPort() );
        setRequestUri( requestUri );
        setReasonPhrase( null );

        setMethod( req.getMethod() );
        if ( req.isSecure() )
        {
            setProtocol( Protocol.HTTPS );
        }
        else
        {
            setProtocol( Protocol.HTTP );
        }

        // TODO: get this out of Request
        setVersion( "1.1" );

        Series<Parameter> headers = super.getRequestHeaders();

        // Copy the headers from the request object
        String headerName;
        String headerValue;
        for ( Enumeration<String> names = req.getHeaderNames(); names.hasMoreElements(); )
        {
            headerName = (String) names.nextElement();
            for ( Enumeration<String> values = req.getHeaders( headerName ); values.hasMoreElements(); )
            {
                headerValue = (String) values.nextElement();
                headers.add( new Parameter( headerName, headerValue ) );
            }
        }
    }

    /**
     * A constructor that uses the URI from incoming Jetty request taken from httpConnection.
     * 
     * @param httpConnection
     */
    public JettyServerCall( HttpConnection httpConnection )
    {
        this( httpConnection, httpConnection.getRequest().getUri().toString() );
    }

    public ReadableByteChannel getRequestChannel()
    {
        return null;
    }

    /**
     * Returning InputStream from Jetty request.
     */
    public InputStream getRequestStream()
    {
        try
        {
            return getHttpConnection().getRequest().getInputStream();
        }
        catch ( IOException e )
        {
            return null;
        }
    }

    public WritableByteChannel getResponseChannel()
    {
        return null;
    }

    /**
     * Returning OutputStream from Jetty response.
     */
    public OutputStream getResponseStream()
    {
        try
        {
            return getHttpConnection().getResponse().getOutputStream();
        }
        catch ( IOException e )
        {
            return null;
        }
    }

    /**
     * Overriding sendResponse and making it Jetty specific.
     */
    public void sendResponse( Response response )
        throws IOException
    {
        Parameter header;
        for ( Iterator<Parameter> iter = getResponseHeaders().iterator(); iter.hasNext(); )
        {
            header = iter.next();
            getHttpConnection().getResponse().addHeader( header.getName(), header.getValue() );
        }

        if ( Status.isError( getStatusCode() ) && ( response == null ) )
        {
            try
            {
                getHttpConnection().getResponse().sendError( getStatusCode(), getReasonPhrase() );
            }
            catch ( IOException ioe )
            {
                getLogger().log( Level.WARNING, "Unable to set the response error status", ioe );
            }
        }
        else
        {
            getHttpConnection().getResponse().setStatus( getStatusCode() );
            super.sendResponse( response );
        }

        getHttpConnection().completeResponse();
        getHttpConnection().commitResponse( true );
    }

}
