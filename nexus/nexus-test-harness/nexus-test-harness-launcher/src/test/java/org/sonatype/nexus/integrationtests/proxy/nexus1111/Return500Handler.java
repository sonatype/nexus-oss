/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.proxy.nexus1111;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.restlet.data.Method;

public class Return500Handler
    extends AbstractHandler
{

    public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
        throws IOException,
            ServletException
    {

        if ( request.getMethod().equals( Method.HEAD ) )
        {
            response.setContentType( "text/html" );
            response.setStatus( HttpServletResponse.SC_OK );
            response.getWriter().println( "ok" );
            ( (Request) request ).setHandled( true );
        }
        else
        {
            response.setContentType( "text/html" );
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            response.getWriter().println( "error" );
            ( (Request) request ).setHandled( true );
        }
    }

}
