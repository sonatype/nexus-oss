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

import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.B64Code;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.log.Log;
import org.mortbay.util.StringUtil;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

// Grabbed from BasicAuthenticator in Jetty, and modded to use NxBASIC authType instead.
public class NxBasicAuthenticator
    implements Authenticator
{

    public static final String AUTH_TYPE = "NxBASIC";

    private static final long serialVersionUID = 1L;

    /* ------------------------------------------------------------ */
    /**
     * @return UserPrinciple if authenticated or null if not. If Authentication fails, then the authenticator may have
     *         committed the response as an auth challenge or redirect.
     * @exception IOException
     */
    public Principal authenticate( final UserRealm realm, final String pathInContext, final Request request,
                                   final Response response )
        throws IOException
    {
        // Get the user if we can
        Principal user = null;
        String credentials = request.getHeader( HttpHeaders.AUTHORIZATION );

        if ( credentials != null )
        {
            try
            {
                if ( Log.isDebugEnabled() )
                {
                    Log.debug( "Credentials: " + credentials );
                }
                credentials = credentials.substring( credentials.indexOf( ' ' ) + 1 );
                credentials = B64Code.decode( credentials, StringUtil.__ISO_8859_1 );
                int i = credentials.indexOf( ':' );
                String username = credentials.substring( 0, i );
                String password = credentials.substring( i + 1 );
                user = realm.authenticate( username, password, request );

                if ( user == null )
                {
                    Log.warn( "AUTH FAILURE: user {}", StringUtil.printable( username ) );
                }
                else
                {
                    request.setAuthType( AUTH_TYPE );
                    request.setUserPrincipal( user );
                }
            }
            catch ( Exception e )
            {
                Log.warn( "AUTH FAILURE: " + e.toString() );
                Log.ignore( e );
            }
        }

        // Challenge if we have no user
        if ( user == null && response != null )
        {
            sendChallenge( realm, response );
        }

        return user;
    }

    /* ------------------------------------------------------------ */
    public String getAuthMethod()
    {
        return Constraint.__BASIC_AUTH;
    }

    /* ------------------------------------------------------------ */
    public void sendChallenge( final UserRealm realm, final Response response )
        throws IOException
    {
        response.setHeader( HttpHeaders.WWW_AUTHENTICATE, AUTH_TYPE + " realm=\"" + realm.getName()
            + '"' );
        response.sendError( HttpServletResponse.SC_UNAUTHORIZED );
    }

}
