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

/**
 * Initialize logging system on start-up.
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

import org.apache.log4j.PropertyConfigurator;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class LogConfigListener
    implements ServletContextListener
{
    private static final String KEY_LOG_CONFIG_FILE = "plexus.log4j-prop-file";

    private static final String KEY_NEXUS_WORK_DIR = "plexus.nexus-work";

    private static final String RELATIVE_PATH_LOG_CONF = "/conf/log4j.properties";

    private Handler[] originalHandlers;

    public void contextInitialized( ServletContextEvent sce )
    {
        setUpJULHandlerSLF4J();

        String location = getLogConfigLocation();

        ensureLogConfigLocation( location );

        initializeLogConfig( location );
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
        Logger julLogger = LogManager.getLogManager().getLogger( "" );

        Handler[] slf4jHandlers = julLogger.getHandlers();

        for ( Handler handler : slf4jHandlers )
        {
            julLogger.removeHandler( handler );
        }

        for ( Handler handler : originalHandlers )
        {
            julLogger.addHandler( handler );
        }
    }

    private String getLogConfigLocation()
    {
        String location = System.getProperty( KEY_LOG_CONFIG_FILE );

        if ( StringUtils.isEmpty( location ) )
        {
            String workDir = System.getProperty( KEY_NEXUS_WORK_DIR );

            location = new File( workDir, RELATIVE_PATH_LOG_CONF ).getAbsolutePath();

            System.getProperties().put( KEY_LOG_CONFIG_FILE, location );
        }

        return location;
    }

    private void ensureLogConfigLocation( String location )
    {
        File logConfigFile = new File( location );

        if ( logConfigFile.exists() )
        {
            return;
        }

        try
        {
            URL configUrl = this.getClass().getResource( "/META-INF/log/log4j.properties" );

            FileUtils.copyURLToFile( configUrl, logConfigFile );
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( "Could not create default log4j.properties into "
                + logConfigFile.getAbsolutePath(), e );
        }
    }

    private void initializeLogConfig( String location )
    {
        PropertyConfigurator.configure( location );
    }

}
