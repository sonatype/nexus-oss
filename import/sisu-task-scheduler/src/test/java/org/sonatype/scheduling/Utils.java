/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.scheduling;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Utils
{

    public static void awaitTaskState( ScheduledTask<?> task, long timeout, TaskState... states )
    {
        Set<TaskState> stateSet = new HashSet<TaskState>( Arrays.asList( states ) );

        long start = System.currentTimeMillis();
        TaskState state = null;
        do
        {
            state = task.getTaskState();
            if ( stateSet.contains( state ) )
            {
                return;
            }
            try
            {
                Thread.sleep( 10 );
            }
            catch ( InterruptedException e )
            {
                // ignored
            }
        }
        while ( System.currentTimeMillis() - start <= timeout );
        fail( "exceeded timeout while waiting for task " + task + " to transition from state " + state + " into "
            + stateSet );
    }

    public static void awaitZeroTaskCount( Scheduler scheduler, long timeout )
    {
        long start = System.currentTimeMillis();
        int n = 0;
        do
        {
            n = scheduler.getAllTasks().size();
            if ( n <= 0 )
            {
                return;
            }
            try
            {
                Thread.sleep( 10 );
            }
            catch ( InterruptedException e )
            {
                // ignored
            }
        }
        while ( System.currentTimeMillis() - start <= timeout );
        fail( "exceeded timeout while waiting for task map to transition from count " + n + " into 0" );
    }

}
