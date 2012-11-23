/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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


import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;

/**
 * @author Juven Xu
 */
public class PlexusContainerContextListener
    implements ServletContextListener
{
    private static final String KEY_PLEXUS = "plexus";

    PlexusContainerConfigurationUtils plexusContainerConfigurationUtils = new PlexusContainerConfigurationUtils();

    PlexusContainerUtils plexusContainerUtils = new PlexusContainerUtils();

    public void contextInitialized( ServletContextEvent sce )
    {
        ServletContext context = sce.getServletContext();

        ContainerConfiguration plexusContainerConfiguration = this.buildContainerConfiguration( context );

        try
        {
            initizlizePlexusContainer( context, plexusContainerConfiguration );
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

    protected void initizlizePlexusContainer( ServletContext context, ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        PlexusContainer plexusContainer = plexusContainerUtils.startContainer( configuration );

        context.setAttribute( KEY_PLEXUS, plexusContainer );
    }
    
    protected ContainerConfiguration buildContainerConfiguration( ServletContext context )
    {
        return plexusContainerConfigurationUtils
        .buildContainerConfiguration( context );
    }
}
