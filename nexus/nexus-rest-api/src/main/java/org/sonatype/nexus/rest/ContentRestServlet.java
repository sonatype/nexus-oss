/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;

import com.noelios.restlet.ext.servlet.ServletConverter;

public class ContentRestServlet
    extends HttpServlet
{
    private ServletConverter servletConverter;

    public void init()
        throws ServletException
    {
        super.init();

        servletConverter = new ServletConverter( getServletContext() );

        try
        {
            ApplicationContentBridge app = new ApplicationContentBridge( servletConverter.getContext() );
            
            app.setPlexusContainer( getPlexusContainer() );

            app.start();

            servletConverter.setTarget( app );
        }
        catch ( Exception e )
        {
            log( "Error during the starting of the Restlet Application", e );
        }

    }

    protected void service( HttpServletRequest req, HttpServletResponse res )
        throws ServletException,
            IOException
    {
        servletConverter.service( req, res );
    }

    protected PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getServletContext().getAttribute( PlexusConstants.PLEXUS_KEY );
    }

}
