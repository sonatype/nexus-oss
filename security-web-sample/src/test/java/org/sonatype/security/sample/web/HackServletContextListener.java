package org.sonatype.security.sample.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.sonatype.plexus.rest.PlexusContainerContextListener;


public class HackServletContextListener
    extends PlexusContainerContextListener
{

    private static ThreadLocal<PlexusContainer> plexusContainer = new ThreadLocal<PlexusContainer>();
    
    

    @Override
    public void contextDestroyed( ServletContextEvent sce )
    {
        // do nothing
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
