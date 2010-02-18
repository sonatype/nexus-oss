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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;

public abstract class AbstractNexusTestCase
    extends org.sonatype.nexus.proxy.AbstractNexusTestCase
{
    private NexusScheduler nexusScheduler;

    public static final String RUNTIME_CONFIGURATION_KEY = "runtime";

    public static final String NEXUS_APP_CONFIGURATION_KEY = "nexus-app";

    protected NexusConfiguration nexusConfiguration;
    
    private static File runtimeHomeDir = null;
    private static File nexusappHomeDir = null;

    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );
        
        runtimeHomeDir = new File( getPlexusHomeDir(), "runtime" );
        nexusappHomeDir = new File( getPlexusHomeDir(), "nexus-app" );

        ctx.put( RUNTIME_CONFIGURATION_KEY, runtimeHomeDir.getAbsolutePath() );
        ctx.put( NEXUS_APP_CONFIGURATION_KEY, nexusappHomeDir.getAbsolutePath() );
    }

    protected boolean loadConfigurationAtSetUp()
    {
        return true;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        runtimeHomeDir.mkdirs();
        nexusappHomeDir.mkdirs();
        
        nexusScheduler = lookup( NexusScheduler.class );

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
        
        cleanDir( runtimeHomeDir );
        cleanDir( nexusappHomeDir );
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
