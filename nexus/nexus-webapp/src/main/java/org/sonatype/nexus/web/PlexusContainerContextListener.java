/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.PropertyConfigurator;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;

/**
 * 
 * @author Juven Xu
 *
 */
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

            //initialize log4j
            String log4jFile = servletContext.getRealPath( "/" ) + "/WEB-INF/log4j.properties";

            PropertyConfigurator.configure( log4jFile );

            //initialize plexus container
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
