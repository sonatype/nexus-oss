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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.PlexusContainer;
import org.sonatype.nexus.Nexus;

public class NexusBooterListener
    implements ServletContextListener
{

    public void contextInitialized( ServletContextEvent sce )
    {
        try
        {
            PlexusContainer c = (PlexusContainer) sce.getServletContext().getAttribute( "plexus" );

            Nexus nexus = (Nexus) c.lookup( Nexus.class.getName() );

            sce.getServletContext().setAttribute( Nexus.class.getName(), nexus );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Could not initialize Nexus.", e );
        }
    }

    public void contextDestroyed( ServletContextEvent sce )
    {
    }

}
