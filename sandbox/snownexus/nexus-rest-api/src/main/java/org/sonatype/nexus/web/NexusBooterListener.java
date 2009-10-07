/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.PlexusContainer;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.NexusConfiguration;

public class NexusBooterListener
    implements ServletContextListener
{

    private static PlexusContainer plexus;

    public void contextInitialized( ServletContextEvent sce )
    {
        try
        {
            plexus = (PlexusContainer) sce.getServletContext().getAttribute( "plexus" );

            Nexus nexus = plexus.lookup( Nexus.class );
            sce.getServletContext().setAttribute( Nexus.class.getName(), nexus );

            NexusConfiguration nexusConfiguration = plexus.lookup( NexusConfiguration.class );
            sce.getServletContext().setAttribute( NexusConfiguration.class.getName(), nexusConfiguration );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Could not initialize Nexus.", e );
        }
    }

    public void contextDestroyed( ServletContextEvent sce )
    {
        plexus = null;
    }

    public static PlexusContainer getPlexus() {
        return plexus;
    }
}
