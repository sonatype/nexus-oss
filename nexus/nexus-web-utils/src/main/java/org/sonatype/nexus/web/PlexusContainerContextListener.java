package org.sonatype.nexus.web;

import java.io.File;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextFactory;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.PropertiesFileContextFiller;
import org.sonatype.appcontext.SimpleBasedirDiscoverer;

/**
 * This ServeletContextListener boots up Plexus in a webapp environment, if needed. It is safe to have it multiple times
 * executed, since it will create only once, or reuse the found container.
 * 
 * @author cstamas
 */
public class PlexusContainerContextListener
    implements ServletContextListener
{
    public static final String PLEXUS_CONFIG_PARAM = "plexus-config";

    public static final String PLEXUS_PROPERTIES_PARAM = "plexus-properties";

    public static final String DEFAULT_PLEXUS_CONFIG = "/WEB-INF/plexus.xml";

    public static final String DEFAULT_PLEXUS_PROPERTIES = "/WEB-INF/plexus.properties";

    private AppContextFactory appContextFactory = new AppContextFactory();

    private PlexusContainer plexusContainer;

    public void contextInitialized( ServletContextEvent sce )
    {
        ServletContext context = sce.getServletContext();

        // create a container if there is none yet
        if ( context.getAttribute( PlexusConstants.PLEXUS_KEY ) == null )
        {
            try
            {
                AppContext plexusContext = createContainerContext( context );

                ContainerConfiguration plexusConfiguration =
                    new DefaultContainerConfiguration().setName( context.getServletContextName() ).setContainerConfigurationURL(
                                                                                                                                 buildConfigurationURL(
                                                                                                                                                        context,
                                                                                                                                                        PLEXUS_CONFIG_PARAM,
                                                                                                                                                        DEFAULT_PLEXUS_CONFIG ) ).setContext(
                                                                                                                                                                                              plexusContext );

                plexusContainer = new DefaultPlexusContainer( plexusConfiguration );

                context.setAttribute( PlexusConstants.PLEXUS_KEY, plexusContainer );
            }
            catch ( Exception e )
            {
                sce.getServletContext().log( "Could not start Plexus container!", e );

                throw new IllegalStateException( "Could not start Plexus container!", e );
            }
        }
    }

    public void contextDestroyed( ServletContextEvent sce )
    {
        if ( plexusContainer != null )
        {
            plexusContainer.dispose();
        }
    }

    // ==

    protected AppContext createContainerContext( ServletContext context )
        throws Exception
    {
        String baseDir = context.getRealPath( "/WEB-INF" );

        File basedirFile = null;

        if ( !StringUtils.isEmpty( baseDir ) )
        {
            context.log( "Setting Plexus basedir context variable to: " + baseDir );

            basedirFile = new File( baseDir );
        }
        else
        {
            context.log( "CANNOT set Plexus basedir! (are we in unpacked WAR?)" );

            basedirFile = new File( "" );
        }

        AppContextRequest request = appContextFactory.getDefaultAppContextRequest();

        String servletContextName = context.getServletContextName();

        if ( servletContextName != null )
        {
            request.setName( context.getServletContextName() );
        }

        // just pass over the already found basedir
        request.setBasedirDiscoverer( new SimpleBasedirDiscoverer( basedirFile ) );

        // create a properties filler for plexus.properties, that will fail if props file not found
        File containerPropertiesFile = new File( basedirFile, "plexus.properties" );

        PropertiesFileContextFiller plexusPropertiesFiller =
            new PropertiesFileContextFiller( containerPropertiesFile, true );

        // add it to fillers as very 1st resource, and leaving others in
        request.getContextFillers().add( 0, plexusPropertiesFiller );

        AppContext response = appContextFactory.getAppContext( request );

        // put the app booter into context too
        response.put( getClass().getName(), this );

        // put in the basedir for plexus apps backward compat
        response.put( "basedir", response.getBasedir().getAbsolutePath() );

        // wrap it in, to make Plexus friendly
        return response;
    }

    private URL buildConfigurationURL( final ServletContext servletContext, final String paramKey,
                                       final String defaultValue )
    {
        String plexusConfigPath = servletContext.getInitParameter( paramKey );

        if ( plexusConfigPath == null )
        {
            plexusConfigPath = defaultValue;
        }

        URL url = null;

        try
        {
            url = servletContext.getResource( plexusConfigPath );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Could not load plexus configuration from: '" + plexusConfigPath + "'", e );
        }

        servletContext.log( "Loading plexus configuration from: '" + url.toString() + "'" );

        return url;
    }

}
