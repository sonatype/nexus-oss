/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;

/**
 * The proxy handler handles one incoming HTTP request connection. Since this is not a generic HTTP Proxy, and we
 * strieve only to support Maven2, this handler handles only HTTP GET requests meant for proxies (full URL is
 * submitted), and HTTP protocol HTTP/1.0 or HTTP/1.1.
 * 
 * @author cstamas
 */
public class HttpProxyHandler
    extends AbstractLogEnabled
    implements Runnable
{
    private HttpProxyService service;

    private final Socket socket;

    private final HttpProxyPolicy policy;

    public HttpProxyHandler( Logger logger, HttpProxyService service, HttpProxyPolicy policy, Socket socket )
    {
        super();

        enableLogging( logger );

        this.service = service;

        this.policy = policy;

        this.socket = socket;
    }

    public void run()
    {
        BufferedInputStream clientIn = null;
        BufferedOutputStream clientOut = null;
        try
        {
            // client streams (make sure you're using streams that use
            // byte arrays, so things like GIF and JPEG files and file
            // downloads will transfer properly)
            clientIn = new BufferedInputStream( socket.getInputStream() );
            clientOut = new BufferedOutputStream( socket.getOutputStream() );

            HttpRequest clientRequest = new HttpRequest();
            clientRequest.readInput( clientIn );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "HTTP Proxy request for: " + clientRequest.getUri() );
            }

            // we support only HTTP/1.0 or HTTP/1.1 GET requests
            // we support only Maven2
            if ( "GET".equals( clientRequest.getMethod() )
                && ( "HTTP/1.0".equals( clientRequest.getHttpVersion() ) || "HTTP/1.1".equals( clientRequest
                    .getHttpVersion() ) ) )
            {
                HttpResponse clientResponse = new HttpResponse();

                clientResponse.setHttpVersion( clientRequest.getHttpVersion() );

                URL requestedUrl = null;

                try
                {
                    requestedUrl = new URL( clientRequest.getUri() );

                    NexusURLResolver nexusURLResolver = service.getNexusURLResolver();

                    URL resolvedUrl = nexusURLResolver.resolve( requestedUrl );

                    if ( resolvedUrl == null && HttpProxyPolicy.PASS_THRU.equals( policy ) )
                    {
                        // it is not a nexus known repos, but we are allowed to make out-bound proxying
                        resolvedUrl = requestedUrl;
                    }

                    if ( resolvedUrl != null )
                    {
                        int port = resolvedUrl.getDefaultPort();

                        if ( resolvedUrl.getPort() != -1 )
                        {
                            port = resolvedUrl.getPort();
                        }

                        Socket serverSocket = new Socket( resolvedUrl.getHost(), port );
                        
                        //Hopefully this will resolve the problem of using up all teh system sockets
                        //and running out of resources
                        serverSocket.setSoLinger( false, 0 );

                        serverSocket.setSoTimeout( DefaultHttpProxyService.DEFAULT_TIMEOUT );

                        BufferedOutputStream serverOut = new BufferedOutputStream( serverSocket.getOutputStream() );

                        clientRequest.setUri( resolvedUrl.getFile() );

                        clientRequest.getHeaders().put( "Host", resolvedUrl.getHost() );

                        clientRequest.getHeaders().put( "Connection", "close" );

                        clientRequest.getHeaders().put( "Via", "Nexus HTTP Proxy 0.1" );

                        clientRequest.write( serverOut );

                        BufferedInputStream serverIn = new BufferedInputStream( serverSocket.getInputStream() );

                        clientResponse.readInput( serverIn );

                        clientResponse.getHeaders().put( "Via", "Nexus HTTP Proxy 0.1" );

                        clientResponse.write( clientOut );

                        serverOut.close();

                        serverIn.close();

                        serverSocket.close();
                    }
                    else
                    {
                        clientResponse.setStatusCode( HttpResponse.FORBIDDEN );

                        clientResponse.setReasonPhrase( "Forbidden: out-bound proxying is forbidden by policy" );

                        clientResponse.write( clientOut );

                        getLogger().info(
                            "OutBound proxying requested, but policy forbids it: " + requestedUrl.toString() );
                    }
                }
                catch ( MalformedURLException e )
                {
                    clientResponse.setStatusCode( HttpResponse.BAD_REQUEST );

                    clientResponse.setReasonPhrase( "Bad request: the request should contain the full URL!" );

                    clientResponse.write( clientOut );

                    getLogger().info( "Bad request for proxying: ", e );
                }
            }
            else
            {
                HttpResponse clientResponse = new HttpResponse();

                clientResponse.setHttpVersion( clientRequest.getHttpVersion() );

                clientResponse.setStatusCode( HttpResponse.NOT_IMPLEMENTED );

                clientResponse.setReasonPhrase( "Not Implemented" );

                clientResponse.write( clientOut );
            }
        }
        catch ( Exception e )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Error in HttpProxyHandler: ", e );
            }
        }
        finally
        {
            // close all the client streams so we can listen again
            if ( clientOut != null )
            {
                try
                {
                    clientOut.close();
                }
                catch ( IOException e )
                {
                }
            }
            if ( clientIn != null )
            {
                try
                {
                    clientIn.close();
                }
                catch ( IOException e )
                {
                }
            }
            if ( socket != null )
            {
                try
                {
                    socket.close();
                }
                catch ( IOException e )
                {
                }
            }
        }
    }

}
