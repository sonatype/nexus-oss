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
package org.sonatype.nexus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;

public abstract class AbstractNexusTestCase
    extends PlexusTestCase
{
    private NexusScheduler nexusScheduler;

    protected static final String PROXY_SERVER_PORT = "proxy.server.port";

    public static final String RUNTIME_CONFIGURATION_KEY = "runtime";

    public static final String WORK_CONFIGURATION_KEY = "nexus-work";

    public static final String APP_CONFIGURATION_KEY = "nexus-app";

    public static final String APPS_CONFIGURATION_KEY = "apps";

    public static final String APPLICATION_CONF_KEY = "application-conf";

    public static final String APPLICATION_CONFIG_KEY = APPLICATION_CONF_KEY;

    public static final String SECURITY_CONFIG_KEY = "security-xml-file";

    protected static final File PLEXUS_HOME = new File( getBasedir(), "target/plexus-home" );

    protected static final File WORK_HOME = new File( PLEXUS_HOME, "nexus-work" );

    protected static final File APP_HOME = new File( PLEXUS_HOME, "nexus-app" );

    protected static final File CONF_HOME = new File( WORK_HOME, "conf" );

    protected NexusConfiguration nexusConfiguration;

    @Override
    protected void customizeContext( Context ctx )
    {
        ctx.put( APPS_CONFIGURATION_KEY, PLEXUS_HOME.getAbsolutePath() );

        ctx.put( WORK_CONFIGURATION_KEY, WORK_HOME.getAbsolutePath() );

        ctx.put( APP_CONFIGURATION_KEY, APP_HOME.getAbsolutePath() );

        ctx.put( RUNTIME_CONFIGURATION_KEY, PLEXUS_HOME.getAbsolutePath() );

        ctx.put( SECURITY_CONFIG_KEY, CONF_HOME.getAbsolutePath() + "/security.xml" );

        ctx.put( APPLICATION_CONF_KEY, CONF_HOME.getAbsolutePath() );

        ctx.put( PROXY_SERVER_PORT, String.valueOf( allocatePort() ) );
    }

    private int allocatePort()
    {
        ServerSocket ss;
        try
        {
            ss = new ServerSocket( 0 );
        }
        catch ( IOException e )
        {
            return 0;
        }
        int port = ss.getLocalPort();
        try
        {
            ss.close();
        }
        catch ( IOException e )
        {
            // does it matter?
            fail( "Error allocating port " + e.getMessage() );
        }
        return port;
    }

    protected String getNexusConfiguration()
    {
        return CONF_HOME + "/nexus.xml";
    }

    protected String getNexusSecurityConfiguration()
    {
        return CONF_HOME + "/security.xml";
    }

    protected void copyDefaultConfigToPlace()
        throws IOException
    {
        this.copyResource( "/META-INF/nexus/nexus.xml", getNexusConfiguration() );
    }

    protected void copyDefaultSecurityConfigToPlace()
        throws IOException
    {
        this.copyResource( "/META-INF/security/security.xml", getNexusSecurityConfiguration() );
    }

    protected void copyResource( String resource, String dest )
        throws IOException
    {
        InputStream stream = null;
        try
        {
            // make the directory
            new File( dest ).getParentFile().mkdirs();
            stream = getClass().getResourceAsStream( resource );
            IOUtil.copy( stream, new FileOutputStream( dest ) );
        }
        finally
        {
            IOUtil.close( stream );
        }
    }

    protected boolean loadConfigurationAtSetUp()
    {
        return true;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        nexusScheduler = lookup( NexusScheduler.class );

        FileUtils.deleteDirectory( PLEXUS_HOME );

        PLEXUS_HOME.mkdirs();
        WORK_HOME.mkdirs();
        CONF_HOME.mkdirs();

        super.setUp();

        if ( loadConfigurationAtSetUp() )
        {
            System.out.println( "== Shutting down SECURITY!" );

            nexusConfiguration = this.lookup( NexusConfiguration.class );

            nexusConfiguration.loadConfiguration( false );

            // TODO: SEE WHY IS SEC NOT STARTING? (Max, JSec changes)
            nexusConfiguration.setSecurityEnabled( false );

            nexusConfiguration.saveConfiguration();

            System.out.println( "== Shutting down SECURITY!" );
        }
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        waitForTasksToStop();

        super.tearDown();

        FileUtils.deleteDirectory( PLEXUS_HOME );
    }

    protected void killActiveTasks()
        throws Exception
    {
        Map<String, List<ScheduledTask<?>>> taskMap = nexusScheduler.getActiveTasks();

        for ( List<ScheduledTask<?>> taskList : taskMap.values() )
        {
            for ( ScheduledTask<?> task : taskList )
            {
                task.cancel();
            }
        }
    }

    protected void waitForTasksToStop()
        throws Exception
    {
        if ( nexusScheduler == null )
        {
            return;
        }

        // Give task a chance to start
        Thread.sleep( 50 );

        int counter = 0;

        while ( nexusScheduler.getActiveTasks().size() > 0 )
        {
            Thread.sleep( 100 );
            counter++;

            if ( counter > 300 )
            {
                System.out.println( "TIMEOUT WAITING FOR TASKS TO COMPLETE!!!  Will kill them." );
                printActiveTasks();
                killActiveTasks();
                break;
            }
        }
    }

    protected void printActiveTasks()
        throws Exception
    {
        Map<String, List<ScheduledTask<?>>> taskMap = nexusScheduler.getActiveTasks();

        for ( List<ScheduledTask<?>> taskList : taskMap.values() )
        {
            for ( ScheduledTask<?> task : taskList )
            {
                System.out.println( task.getName() + " with id " + task.getId() + " is in state "
                    + task.getTaskState().toString() );
            }
        }
    }

    protected LoggerManager getLoggerManager()
        throws ComponentLookupException
    {
        return getContainer().lookup( LoggerManager.class );
    }

    protected boolean contentEquals( File f1, File f2 )
        throws IOException
    {
        return contentEquals( new FileInputStream( f1 ), new FileInputStream( f2 ) );
    }

    /**
     * Both s1 and s2 will be closed.
     */
    protected boolean contentEquals( InputStream s1, InputStream s2 )
        throws IOException
    {
        try
        {
            return IOUtil.contentEquals( s1, s2 );
        }
        finally
        {
            IOUtil.close( s1 );
            IOUtil.close( s2 );
        }
    }

}
