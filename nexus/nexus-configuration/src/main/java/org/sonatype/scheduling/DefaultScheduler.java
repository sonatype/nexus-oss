/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.scheduling.iterators.ScheduleIterator;
import org.sonatype.scheduling.schedules.Schedule;

/**
 * A simple facade to ScheduledThreadPoolExecutor as Plexus component.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultScheduler
    extends AbstractLogEnabled
    implements Scheduler, Contextualizable, Startable
{
    private PlexusContainer plexusContainer;

    private PlexusThreadFactory plexusThreadFactory;

    private ScheduledThreadPoolExecutor scheduledExecutorService;

    private Map<String, List<SubmittedTask>> tasksMap;

    public void contextualize( Context context )
        throws ContextException
    {
        plexusContainer = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    public void start()
        throws StartingException
    {
        tasksMap = new HashMap<String, List<SubmittedTask>>();

        plexusThreadFactory = new PlexusThreadFactory( plexusContainer );

        scheduledExecutorService = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(
            10,
            plexusThreadFactory );
    }

    public void stop()
        throws StoppingException
    {
        getScheduledExecutorService().shutdown();

        try
        {
            boolean stopped = getScheduledExecutorService().awaitTermination( 15, TimeUnit.SECONDS );

            if ( !stopped )
            {
                List<Runnable> queueds = getScheduledExecutorService().shutdownNow();

                getLogger().warn( "Scheduler shut down with " + queueds.size() + " tasks queued." );
            }
        }
        catch ( InterruptedException e )
        {
            getLogger().info( "Termination interrupted", e );
        }
    }

    public PlexusThreadFactory getPlexusThreadFactory()
    {
        return plexusThreadFactory;
    }

    public ScheduledThreadPoolExecutor getScheduledExecutorService()
    {
        return scheduledExecutorService;
    }

    protected void addToTasksMap( SubmittedTask task )
    {
        synchronized ( tasksMap )
        {
            if ( !tasksMap.containsKey( task.getType() ) )
            {
                tasksMap.put( task.getType(), new ArrayList<SubmittedTask>() );
            }
            tasksMap.get( task.getType() ).add( task );
        }
    }

    protected void removeFromTasksMap( SubmittedTask task )
    {
        synchronized ( tasksMap )
        {
            if ( tasksMap.containsKey( task.getType() ) )
            {
                tasksMap.get( task.getType() ).remove( task );

                if ( tasksMap.get( task.getType() ).size() == 0 )
                {
                    tasksMap.remove( task.getType() );
                }
            }
        }
    }

    public SubmittedTask submit( Runnable runnable )
    {
        DefaultCallableTask<Object> drt = new DefaultCallableTask<Object>( runnable.getClass().getName(), Executors
            .callable( runnable ), null, this, null );

        addToTasksMap( drt );

        drt.start();

        return drt;
    }

    public IteratingTask iterate( Runnable runnable, ScheduleIterator iterator )
    {
        DefaultCallableTask<Object> drt = new DefaultCallableTask<Object>( runnable.getClass().getName(), Executors
            .callable( runnable ), iterator, this, null );

        addToTasksMap( drt );

        drt.start();

        return drt;
    }

    public ScheduledTask schedule( Runnable runnable, Schedule schedule )
    {
        DefaultCallableTask<Object> drt = new DefaultCallableTask<Object>( runnable.getClass().getName(), Executors
            .callable( runnable ), schedule.getIterator(), this, schedule );

        addToTasksMap( drt );

        drt.start();

        return drt;
    }

    public <T> SubmittedCallableTask<T> submit( Callable<T> callable )
    {
        DefaultCallableTask<T> dct = new DefaultCallableTask<T>(
            callable.getClass().getName(),
            callable,
            null,
            this,
            null );

        addToTasksMap( dct );

        dct.start();

        return dct;
    }

    public <T> IteratingCallableTask<T> iterate( Callable<T> callable, ScheduleIterator iterator )
    {
        DefaultCallableTask<T> dct = new DefaultCallableTask<T>(
            callable.getClass().getName(),
            callable,
            iterator,
            this,
            null );

        addToTasksMap( dct );

        dct.start();

        return dct;
    }

    public <T> ScheduledCallableTask<T> schedule( Callable<T> callable, Schedule schedule )
    {
        DefaultCallableTask<T> dct = new DefaultCallableTask<T>( callable.getClass().getName(), callable, schedule
            .getIterator(), this, schedule );

        addToTasksMap( dct );

        dct.start();

        return dct;
    }

    public Map<String, List<SubmittedTask>> getScheduledTasks()
    {
        Map<String, List<SubmittedTask>> result = null;

        // create a "snapshots" of active tasks
        synchronized ( tasksMap )
        {
            result = new HashMap<String, List<SubmittedTask>>( tasksMap.size() );

            List<SubmittedTask> tasks = null;

            for ( String cls : tasksMap.keySet() )
            {
                tasks = new ArrayList<SubmittedTask>();

                for ( SubmittedTask task : tasksMap.get( cls ) )
                {
                    if ( task.getTaskState().isActive() )
                    {
                        tasks.add( task );
                    }
                }

                if ( tasks.size() > 0 )
                {
                    result.put( cls, tasks );
                }
            }
        }

        return result;
    }

}
