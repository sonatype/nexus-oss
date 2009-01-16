/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
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
 * @author Juven Xu
 */
public class PlexusContainerContextListener
    implements ServletContextListener
{
    private static final String KEY_PLEXUS = "plexus";

    private static final String KEY_LOG_CONFIG_FILE = "plexus.log4j-prop-file";

    PlexusContainerConfigurationUtils plexusContainerConfigurationUtils = new PlexusContainerConfigurationUtils();

    PlexusContainerUtils plexusContainerUtils = new PlexusContainerUtils();

    public void contextInitialized( ServletContextEvent sce )
    {
        ServletContext context = sce.getServletContext();

        ContainerConfiguration plexusContainerConfiguration = plexusContainerConfigurationUtils
            .buildContainerConfiguration( context );

        NexusWorkDirUtils.setUpNexusWorkDir( plexusContainerConfiguration.getContext() );

        initializeLogConfig( context );

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

    private void initializeLogConfig( ServletContext context )
    {
        String logConfigFilePath = context.getRealPath( "/" ) + "/WEB-INF/log4j.properties";

        // when we want to configure log dynamically, this value is used to local the log configuration file
        System.getProperties().put( KEY_LOG_CONFIG_FILE, logConfigFilePath );

        PropertyConfigurator.configure( logConfigFilePath );
    }

    private void initizlizePlexusContainer( ServletContext context, ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        PlexusContainer plexusContainer = plexusContainerUtils.startContainer( configuration );

        context.setAttribute( KEY_PLEXUS, plexusContainer );
    }
}
