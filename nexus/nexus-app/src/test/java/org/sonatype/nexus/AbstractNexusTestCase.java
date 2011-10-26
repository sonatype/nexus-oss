/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
