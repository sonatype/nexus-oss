package org.sonatype.security.web.testapp;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.sonatype.plexus.rest.PlexusContainerContextListener;


public class HackedPlexusContainerContextListener
    extends PlexusContainerContextListener
{

    private static ThreadLocal<PlexusContainer> plexusContainer = new ThreadLocal<PlexusContainer>();
    
    
//    @Override
//    public void contextInitialized( ServletContextEvent sce )
//    {
//        SLF4JBridgeHandler.install();
//        super.contextInitialized( sce );
//    }

    @Override
    public void contextDestroyed( ServletContextEvent sce )
    {
        SLF4JBridgeHandler.uninstall();
    }


    @Override
    protected void initizlizePlexusContainer( ServletContext context, ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        context.setAttribute( "plexus", this.plexusContainer.get());
    }


    public PlexusContainer getPlexusContainer()
    {
        return plexusContainer.get();
    }


    public void setPlexusContainer( PlexusContainer plexusContainer )
    {
        this.plexusContainer.set( plexusContainer );
    }

}
