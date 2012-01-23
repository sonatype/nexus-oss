/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
