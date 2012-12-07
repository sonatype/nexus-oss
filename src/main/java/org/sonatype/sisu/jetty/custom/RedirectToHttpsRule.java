/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.sisu.jetty.custom;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * Simple rule to perform a HTTP to HTTPS redirect. Usable for testing and such, but Jetty has real solutions and
 * handlers to perform this.
 * 
 * @author jdcasey
 */
public class RedirectToHttpsRule
    extends Rule
{
    private static final Logger LOG = Log.getLogger( RedirectToHttpsRule.class.getName() );

    private Integer httpsPort;

    public RedirectToHttpsRule()
    {
        setTerminating( true );
    }

    public int getHttpsPort()
    {
        return httpsPort;
    }

    public void setHttpsPort( int httpsPort )
    {
        LOG.debug( "HTTPS port set to: {}", httpsPort, null );

        this.httpsPort = httpsPort;
    }

    @Override
    public String matchAndApply( String target, HttpServletRequest request, HttpServletResponse response )
        throws IOException
    {
        StringBuffer requestURL = request.getRequestURL();
        LOG.debug( "Original URL: {}", requestURL, null );

        if ( !requestURL.toString().startsWith( "https" ) )
        {
            if ( "POST".equals( request.getMethod() ) )
            {
                response.sendError( HttpServletResponse.SC_BAD_REQUEST, "POST to HTTP not supported. Please use HTTPS"
                    + ( httpsPort == null ? "" : " (Port: " + httpsPort + ")" ) + " instead." );
                return target;
            }

            URL url = new URL( requestURL.toString() );

            StringBuilder result = new StringBuilder();
            result.append( "https://" ).append( url.getHost() );

            if ( httpsPort != null )
            {
                result.append( ':' ).append( httpsPort );
            }

            result.append( url.getPath() );

            String queryString = request.getQueryString();
            if ( queryString != null )
            {
                LOG.debug( "Adding query string to redirect: {}", queryString, null );
                result.append( '?' ).append( queryString );
            }

            LOG.debug( "Redirecting to URL: {}", result, null );
            response.sendRedirect( result.toString() );
            return target;
        }
        else
        {
            LOG.debug( "NOT redirecting. Already HTTPS: {}", requestURL, null );
            return null;
        }
    }
}
