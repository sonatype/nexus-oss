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
 * Initialize logging system on start-up. Using this class assumes you are using Log4j!
 * 
 * @author juven
 */
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class LogConfigListener
    implements ServletContextListener
{
    private static final String KEY_LOG_CONFIG_DIR = "plexus.log-config-dir";

    private static final String KEY_NEXUS_WORK_DIR = "plexus.nexus-work";

    private static final String LOG_CONF_RELATIVE_DIR = "conf";

    private static final String LOG_CONF = "logback.xml";

    private static final String LOG_CONF_PROPS = "logback.properties";

    private Handler[] originalHandlers;

    public void contextInitialized( ServletContextEvent sce )
    {
        setUpJULHandlerSLF4J();

        String logConfigDir = getLogConfigDir();

        ensureLogConfigLocation( logConfigDir );

        initializeLogConfig( logConfigDir );
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

        for ( Handler handler : originalHandlers )
        {
            julLogger.addHandler( handler );
        }
    }

    private String getLogConfigDir()
    {
        String logConfigDir = System.getProperty( KEY_LOG_CONFIG_DIR );

        if ( StringUtils.isEmpty( logConfigDir ) )
        {
            logConfigDir = new File( System.getProperty( KEY_NEXUS_WORK_DIR ), LOG_CONF_RELATIVE_DIR ).getAbsolutePath();

            System.getProperties().put( KEY_LOG_CONFIG_DIR, logConfigDir );
        }

        return logConfigDir;
    }

    private void ensureLogConfigLocation( String logConfigDir )
    {
        File logConfigFile = new File( logConfigDir, LOG_CONF );
        File logConfigPropsFile = new File( logConfigDir, LOG_CONF_PROPS );

        if ( !logConfigFile.exists() )
        {
            try
            {
                URL configUrl = this.getClass().getResource( "/META-INF/log/" + LOG_CONF );

                FileUtils.copyURLToFile( configUrl, logConfigFile );
            }
            catch ( IOException e )
            {
                throw new IllegalStateException( "Could not create default logback.xml into "
                    + logConfigFile.getAbsolutePath(), e );
            }
        }
        if ( !logConfigPropsFile.exists() )
        {
            try
            {
                URL configUrl = this.getClass().getResource( "/META-INF/log/" + LOG_CONF_PROPS );

                FileUtils.copyURLToFile( configUrl, logConfigPropsFile );
            }
            catch ( IOException e )
            {
                throw new IllegalStateException( "Could not create default logback.properties into "
                    + logConfigFile.getAbsolutePath(), e );
            }
        }

    }

    private void initializeLogConfig( String logConfigDir )
    {
        // PropertyConfigurator.configure( location );
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        try
        {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext( lc );
            lc.reset();
            configurator.doConfigure( new File( logConfigDir, LOG_CONF ) );
        }
        catch ( JoranException je )
        {
            je.printStackTrace();
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings( lc );
    }

}
