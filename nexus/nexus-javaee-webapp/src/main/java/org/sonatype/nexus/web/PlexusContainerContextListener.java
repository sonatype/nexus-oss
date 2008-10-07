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

package org.sonatype.nexus.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;

public class PlexusContainerContextListener
    implements ServletContextListener
{
    private static final String KEY_PLEXUS = "plexus";

    PlexusContainerConfigurationUtils plexusContainerConfigurationUtils = new PlexusContainerConfigurationUtils();

    PlexusContainerUtils plexusContainerUtils = new PlexusContainerUtils();

    public void contextInitialized( ServletContextEvent sce )
    {
        ServletContext servletContext = sce.getServletContext();

        try
        {
            ContainerConfiguration plexusContainerConfiguration = plexusContainerConfigurationUtils
                .buildContainerConfiguration( servletContext );

            NexusWorkDirUtils.setUpNexusWorkDir( plexusContainerConfiguration.getContext() );

            PlexusContainer plexusContainer = plexusContainerUtils.startContainer( plexusContainerConfiguration );

            servletContext.setAttribute( KEY_PLEXUS, plexusContainer );
        }
        catch ( PlexusContainerException e )
        {
            throw new IllegalStateException( "Could start plexus container", e );
        }
    }

    public void contextDestroyed( ServletContextEvent sce )
    {
        plexusContainerUtils.stopContainer();
    }
}
