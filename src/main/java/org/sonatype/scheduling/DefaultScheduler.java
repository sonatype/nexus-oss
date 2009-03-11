/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.scheduling.schedules.RunNowSchedule;
import org.sonatype.scheduling.schedules.Schedule;

/**
 * A simple facade to ScheduledThreadPoolExecutor as Plexus component.
 * 
 * @author cstamas
 */
@Component( role = Scheduler.class )
public class DefaultScheduler
    extends AbstractLogEnabled
    implements Scheduler, Startable
{
    @Requirement
    private PlexusContainer plexusContainer;

    @Requirement
    private TaskConfigManager taskConfig;

    private PlexusThreadFactory plexusThreadFactory;

    private ScheduledThreadPoolExecutor scheduledExecutorService;

    private Map<String, List<ScheduledTask<?>>> tasksMap;

    private AtomicInteger idGen = new AtomicInteger( 0 );

    private int threadPriority = Thread.MIN_PRIORITY;

    public void start()
        throws StartingException
    {
        tasksMap = new HashMap<String, List<ScheduledTask<?>>>();

        plexusThreadFactory = new PlexusThreadFactory( plexusContainer, threadPriority );

        scheduledExecutorService = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(
            20,
            plexusThreadFactory );

        taskConfig.initializeTasks( this );
    }

    public void stop()
        throws StoppingException
    {
        getScheduledExecutorService().shutdown();

        try
        {
            boolean stopped = getScheduledExecutorService().awaitTermination( 1, TimeUnit.SECONDS );

            if ( !stopped )
            {
                Map<String, List<ScheduledTask<?>>> runningTasks = getRunningTasks();

                if ( runningTasks.size() > 0 )
                {
                    getScheduledExecutorService().shutdownNow();

                    getLogger().warn( "Scheduler shut down forcedly with tasks running." );
                }
                else
                {
                    getLogger().info( "Scheduler shut down cleanly with tasks scheduled." );
                }
            }
        }
        catch ( InterruptedException e )
        {
            getLogger().info( "Termination interrupted", e );
        }
    }

    public SchedulerTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException
    {
        return taskConfig.createTaskInstance( taskType );
    }

    public <T> T createTaskInstance( Class<T> taskType )
        throws IllegalArgumentException
    {
        return taskConfig.createTaskInstance( taskType );
    }

    public PlexusThreadFactory getPlexusThreadFactory()
    {
        return plexusThreadFactory;
    }

    public ScheduledThreadPoolExecutor getScheduledExecutorService()
    {
        return scheduledExecutorService;
    }

    protected <T> void addToTasksMap( ScheduledTask<T> task, boolean store )
    {
        synchronized ( tasksMap )
        {
            if ( !tasksMap.containsKey( task.getType() ) )
            {
                tasksMap.put( task.getType(), new ArrayList<ScheduledTask<?>>() );
            }

            tasksMap.get( task.getType() ).add( task );

            if ( store )
            {
                taskConfig.addTask( task );
            }
        }
    }

    protected void taskRescheduled( ScheduledTask<?> task )
    {
        synchronized ( tasksMap )
        {
            taskConfig.addTask( task );
        }
    }

    protected <T> void removeFromTasksMap( ScheduledTask<T> task )
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

            taskConfig.removeTask( task );
        }
    }

    protected String generateId()
    {
        synchronized ( tasksMap )
        {
            String id;

            if ( idGen.get() == 0 )
            {
                ArrayList<Integer> list = new ArrayList<Integer>();

                for ( List<ScheduledTask<?>> l : tasksMap.values() )
                {
                    for ( ScheduledTask<?> s : l )
                    {
                        list.add( Integer.parseInt( s.getId() ) );
                    }
                }

                Collections.sort( list );

                if ( list.size() > 0 )
                {
                    idGen.set( list.get( list.size() - 1 ) );
                }

                id = String.valueOf( idGen.incrementAndGet() );
            }
            else
            {
                id = String.valueOf( idGen.incrementAndGet() );
            }

            return id;
        }
    }

    public <T> ScheduledTask<T> initialize( String id, String name, String type, Callable<T> callable,
        Schedule schedule, Map<String, String> taskParams )
    {
        return schedule( id, name, type, callable, schedule, taskParams, false );
    }

    public ScheduledTask<Object> submit( String name, Runnable runnable, Map<String, String> taskParams )
    {
        return schedule( name, runnable, new RunNowSchedule(), taskParams );
    }

    public ScheduledTask<Object> schedule( String name, Runnable runnable, Schedule schedule,
        Map<String, String> taskParams )
    {
        // use the name of the class as the type.
        return schedule(
            name,
            runnable.getClass().getSimpleName(),
            Executors.callable( runnable ),
            schedule,
            taskParams );
    }

    public <T> ScheduledTask<T> submit( String name, Callable<T> callable, Map<String, String> taskParams )
    {
        return schedule( name, callable, new RunNowSchedule(), taskParams );
    }

    public <T> ScheduledTask<T> schedule( String name, Callable<T> callable, Schedule schedule,
        Map<String, String> taskParams )
    {
        return schedule( name, callable.getClass().getSimpleName(), callable, schedule, taskParams );
    }

    protected <T> ScheduledTask<T> schedule( String name, String type, Callable<T> callable, Schedule schedule,
        Map<String, String> taskParams )
    {
        return schedule( generateId(), name, type, callable, schedule, taskParams, true );
    }

    protected <T> ScheduledTask<T> schedule( String id, String name, String type, Callable<T> callable,
        Schedule schedule, Map<String, String> taskParams, boolean store )
    {
        DefaultScheduledTask<T> dct = new DefaultScheduledTask<T>( id, name, type, this, callable, schedule, taskParams );

        addToTasksMap( dct, store );

        dct.start();

        return dct;
    }

    public <T> ScheduledTask<T> updateSchedule( ScheduledTask<T> task )
        throws RejectedExecutionException,
            NullPointerException
    {
        // Simply add the task to config, will find existing by id, remove, then store new
        taskConfig.addTask( task );

        return task;
    }

    public Map<String, List<ScheduledTask<?>>> getActiveTasks()
    {
        Map<String, List<ScheduledTask<?>>> result = getAllTasks();

        List<ScheduledTask<?>> tasks = null;

        // filter for activeOrSubmitted
        for ( Iterator<String> c = result.keySet().iterator(); c.hasNext(); )
        {
            String cls = c.next();

            tasks = result.get( cls );

            for ( Iterator<ScheduledTask<?>> i = tasks.iterator(); i.hasNext(); )
            {
                ScheduledTask<?> task = i.next();

                if ( !task.getTaskState().isActiveOrSubmitted() )
                {
                    i.remove();
                }
            }

            if ( tasks.isEmpty() )
            {
                c.remove();
            }
        }

        return result;
    }

    public Map<String, List<ScheduledTask<?>>> getRunningTasks()
    {
        Map<String, List<ScheduledTask<?>>> result = getAllTasks();

        List<ScheduledTask<?>> tasks = null;

        // filter for RUNNING
        for ( Iterator<String> c = result.keySet().iterator(); c.hasNext(); )
        {
            String cls = c.next();

            tasks = result.get( cls );

            for ( Iterator<ScheduledTask<?>> i = tasks.iterator(); i.hasNext(); )
            {
                ScheduledTask<?> task = i.next();

                if ( !TaskState.RUNNING.equals( task.getTaskState() ) )
                {
                    i.remove();
                }
            }

            if ( tasks.isEmpty() )
            {
                c.remove();
            }
        }

        return result;
    }

    public Map<String, List<ScheduledTask<?>>> getAllTasks()
    {
        Map<String, List<ScheduledTask<?>>> result = null;

        // create a "snapshots" of active tasks
        synchronized ( tasksMap )
        {
            result = new HashMap<String, List<ScheduledTask<?>>>( tasksMap.size() );

            List<ScheduledTask<?>> tasks = null;

            for ( String cls : tasksMap.keySet() )
            {
                tasks = new ArrayList<ScheduledTask<?>>();

                for ( ScheduledTask<?> task : tasksMap.get( cls ) )
                {
                    tasks.add( task );
                }

                if ( tasks.size() > 0 )
                {
                    result.put( cls, tasks );
                }
            }
        }
        return result;
    }

    public ScheduledTask<?> getTaskById( String id )
        throws NoSuchTaskException
    {
        if ( StringUtils.isEmpty( id ) )
        {
            throw new IllegalArgumentException( "The Tasks cannot have null IDs!" );
        }

        Collection<List<ScheduledTask<?>>> activeTasks = getAllTasks().values();

        for ( List<ScheduledTask<?>> tasks : activeTasks )
        {
            for ( ScheduledTask<?> task : tasks )
            {
                if ( task.getId().equals( id ) )
                {
                    return task;
                }
            }
        }

        throw new NoSuchTaskException( id );
    }

}
