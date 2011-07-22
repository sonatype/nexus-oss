/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.web;

/**
 * Initialize logging system on start-up.
 * 
 * @author juven
 * @author adreghiciu@gmail.com
 */
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class LogConfigListener
    implements ServletContextListener
{

    private Handler[] originalHandlers;

    public void contextInitialized( ServletContextEvent sce )
    {
        setUpJULHandlerSLF4J();

        configureLogManager( sce.getServletContext() );
    }

    public void contextDestroyed( ServletContextEvent sce )
    {
        setUpJULHandlerOriginal();
    }

    /**
     * Remove the original JUL handlers, install SLF4J handler
     */
    private void setUpJULHandlerSLF4J()
    {
        Logger julLogger = LogManager.getLogManager().getLogger( "" );

        originalHandlers = julLogger.getHandlers();

        for ( Handler handler : originalHandlers )
        {
            julLogger.removeHandler( handler );
        }

        SLF4JBridgeHandler.install();
    }

    private void setUpJULHandlerOriginal()
    {
        SLF4JBridgeHandler.uninstall();

        Logger julLogger = LogManager.getLogManager().getLogger( "" );

        if ( originalHandlers != null )
        {
            for ( Handler handler : originalHandlers )
            {
                julLogger.addHandler( handler );
            }
        }
    }

    private void configureLogManager( ServletContext sc )
    {
        try
        {
            PlexusContainer plexusContainer = (PlexusContainer) sc.getAttribute( PlexusConstants.PLEXUS_KEY );
            if ( plexusContainer == null )
            {
                throw new IllegalStateException( "Could not find Plexus container in servlet context" );
            }

            org.sonatype.nexus.log.LogManager logManager =
                plexusContainer.lookup( org.sonatype.nexus.log.LogManager.class );

            logManager.configure();
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "Could not lookup LogConfigurationParticipants" );
        }

    }

}
