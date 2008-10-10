package org.sonatype.plexus.rest;

import java.util.Enumeration;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;

import com.noelios.restlet.ext.servlet.ServerServlet;
import com.noelios.restlet.ext.servlet.ServletContextAdapter;
import com.noelios.restlet.ext.servlet.ServletWarClient;

public class PlexusServerServlet
    extends ServerServlet
{
    private static final long serialVersionUID = 2636935931764462049L;

    public PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getServletContext().getAttribute( PlexusConstants.PLEXUS_KEY );
    }

    protected Component createComponent()
    {
        Component result = super.createComponent();

        result.getClients().add( new ServletWarClient( result.getContext(), getServletContext() ) );

        return result;
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
