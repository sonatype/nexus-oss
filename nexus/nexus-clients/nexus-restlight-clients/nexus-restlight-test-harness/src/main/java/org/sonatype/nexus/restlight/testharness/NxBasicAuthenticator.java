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

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.authentication.LoginAuthenticator;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.security.Constraint;

// Grabbed from BasicAuthenticator in Jetty, and modded to use NxBASIC authType instead.
public class NxBasicAuthenticator
    extends LoginAuthenticator
{
    public static final String AUTH_TYPE = "NxBASIC";

    /* ------------------------------------------------------------ */
    /**
     * @see org.eclipse.jetty.security.Authenticator#getAuthMethod()
     */
    public String getAuthMethod()
    {
        return Constraint.__BASIC_AUTH;
    }

    /* ------------------------------------------------------------ */
    /**
     * @see org.eclipse.jetty.security.Authenticator#validateRequest(javax.servlet.ServletRequest, javax.servlet.ServletResponse, boolean)
     */
    public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        String credentials = request.getHeader(HttpHeaders.AUTHORIZATION);

        try
        {
            if (credentials != null)
            {                 
                int space=credentials.indexOf(' ');
                if (space>0)
                {
                    credentials = credentials.substring(space+1);
                    credentials = B64Code.decode(credentials,StringUtil.__ISO_8859_1);
                    int i = credentials.indexOf(':');
                    if (i>0)
                    {
                        String username = credentials.substring(0,i);
                        String password = credentials.substring(i+1);

                        UserIdentity user = _loginService.login(username,password);
                        if (user!=null)
                        {
                            renewSessionOnAuthentication(request,response);
                            return new UserAuthentication(AUTH_TYPE,user);
                        }
                    }
                }
            }

            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, AUTH_TYPE+" realm=\"" + _loginService.getName() + '"');
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return Authentication.SEND_CONTINUE;
        }
        catch (IOException e)
        {
            throw new ServerAuthException(e);
        }
    }

    public boolean secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, User validatedUser) throws ServerAuthException
    {
        return true;
    }
}
