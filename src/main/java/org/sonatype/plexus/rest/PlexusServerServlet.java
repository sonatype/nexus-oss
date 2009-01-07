/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.plexus.rest;

import java.util.Enumeration;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.Application;
import org.restlet.Context;

import com.noelios.restlet.ext.servlet.ServerServlet;
import com.noelios.restlet.ext.servlet.ServletContextAdapter;

public class PlexusServerServlet
    extends ServerServlet
{
    private static final long serialVersionUID = 2636935931764462049L;

    public PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getServletContext().getAttribute( PlexusConstants.PLEXUS_KEY );
    }

    public Application createApplication( Context context )
    {
        Application application = null;

        final String applicationRole = getInitParameter( "role", Application.class.getName() );

        final String applicationRoleHint = getInitParameter( "roleHint", null );

        // Load the application class using the given class name
        try
        {
            if ( applicationRoleHint != null )
            {
                application = (Application) getPlexusContainer().lookup( applicationRole, applicationRoleHint );
            }
            else
            {
                application = (Application) getPlexusContainer().lookup( applicationRole );
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "The PlexusServerServlet couldn't lookup the target component (role='"
                + applicationRole + "', hint='" + applicationRoleHint + "')", e );
        }

        // mimic constructor
        application.setName( getServletConfig().getServletName() );

        application.setContext( new ServletContextAdapter( this, context ) );

        // setting logger explicitly, to override the stupid logger put there by ServletContextAdapter
        application.getContext().setLogger( application.getClass().getName() );

        // --- FROM SUPERCLASS (as is) -- BEGIN
        // Copy all the servlet parameters into the context
        final Context applicationContext = application.getContext();

        String initParam;

        // Copy all the Servlet component initialization parameters
        final javax.servlet.ServletConfig servletConfig = getServletConfig();
        for ( final Enumeration<String> enum1 = servletConfig.getInitParameterNames(); enum1.hasMoreElements(); )
        {
            initParam = enum1.nextElement();

            applicationContext.getParameters().add( initParam, servletConfig.getInitParameter( initParam ) );
        }

        // Copy all the Servlet application initialization parameters
        for ( final Enumeration<String> enum1 = getServletContext().getInitParameterNames(); enum1.hasMoreElements(); )
        {
            initParam = enum1.nextElement();

            applicationContext.getParameters().add( initParam, getServletContext().getInitParameter( initParam ) );
        }
        // --- FROM SUPERCLASS (as is) -- END

        return application;
    }

    /**
     * A "dirty" hack to override the "one ServerServlet per webapp context" limitation of Restlet 1.0.x. This happens
     * since they puts the Application into ServletContext, but using the same key.
     */
    public String getInitParameter( String name, String defaultValue )
    {
        String prefixedName = getServletConfig().getServletName() + "." + name;

        String result = getServletConfig().getInitParameter( prefixedName );

        if ( result == null )
        {
            result = getServletConfig().getServletContext().getInitParameter( prefixedName );
        }

        if ( result == null && defaultValue != null )
        {
            result = getServletConfig().getServletName() + "." + defaultValue;
        }

        return result;
    }
}
