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
package org.sonatype.nexus;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.events.EventInspectorHost;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;

public abstract class AbstractNexusTestCase
    extends org.sonatype.nexus.proxy.AbstractNexusTestCase
{

    private NexusScheduler nexusScheduler;

    private EventInspectorHost eventInspectorHost;

    protected NexusConfiguration nexusConfiguration;

    protected boolean loadConfigurationAtSetUp()
    {
        return true;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusScheduler = lookup( NexusScheduler.class );
        eventInspectorHost = lookup( EventInspectorHost.class );

        if ( loadConfigurationAtSetUp() )
        {
            shutDownSecurity();
        }
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        waitForTasksToStop();

        super.tearDown();
    }

    protected void shutDownSecurity()
        throws Exception
    {
        System.out.println( "== Shutting down SECURITY!" );

        nexusConfiguration = this.lookup( NexusConfiguration.class );

        nexusConfiguration.loadConfiguration( false );

        // TODO: SEE WHY IS SEC NOT STARTING? (Max, JSec changes)
        nexusConfiguration.setSecurityEnabled( false );

        nexusConfiguration.saveConfiguration();

        System.out.println( "== Shutting down SECURITY!" );
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

    protected void wairForAsyncEventsToCalmDown()
        throws Exception
    {
        while ( !eventInspectorHost.isCalmPeriod() )
        {
            Thread.sleep( 100 );
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

}
