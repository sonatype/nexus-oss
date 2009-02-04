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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.ManualRunSchedule;
import org.sonatype.scheduling.schedules.Schedule;

public class DefaultScheduledTask<T>
    implements ScheduledTask<T>, Callable<T>
{
    private final String id;

    private String name;

    private final String type;

    private final DefaultScheduler scheduler;

    private final Callable<T> callable;

    private TaskState taskState;

    private Date scheduledAt;

    private Future<T> future;

    private Throwable throwable;

    private boolean enabled;

    private Date lastRun;

    private Date nextRun;

    private List<T> results;

    private Schedule schedule;

    private SchedulerIterator scheduleIterator;

    private Map<String, String> taskParams;

    boolean manualRun;

    public DefaultScheduledTask( String id, String name, String type, DefaultScheduler scheduler,
        Callable<T> callable, Schedule schedule, Map<String, String> taskParams )
    {
        super();

        this.id = id;

        this.name = name;

        this.type = type;

        this.scheduler = scheduler;

        this.callable = callable;

        this.taskState = TaskState.SUBMITTED;

        this.enabled = true;

        this.results = new ArrayList<T>();

        this.schedule = schedule;

        this.scheduleIterator = null;

        this.taskParams = taskParams;

        this.nextRun = null;

        this.manualRun = false;
    }
    
    public boolean isExposed()
    {
        if ( callable != null 
            && SchedulerTask.class.isAssignableFrom( callable.getClass() ) )
        {
            return ( ( SchedulerTask<T> ) callable ).isExposed();
        }
        
        return false;
    }

    protected void start()
    {
        this.scheduledAt = new Date();

        setFuture( reschedule() );
    }

    protected Future<T> getFuture()
    {
        return future;
    }

    protected void setFuture( Future<T> future )
    {
        this.future = future;
    }

    protected DefaultScheduler getScheduler()
    {
        return scheduler;
    }

    protected void setTaskState( TaskState state )
    {
        if ( !getTaskState().isEndingState() )
        {
            this.taskState = state;
        }
    }

    protected void setBrokenCause( Throwable e )
    {
        this.throwable = e;
    }
    
    protected Callable<T> getCallable()
    {
        return callable;
    }

    protected boolean isManualRunScheduled()
    {
        return ManualRunSchedule.class.isAssignableFrom( getSchedule().getClass() );
    }

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public TaskState getTaskState()
    {
        return taskState;
    }

    public Date getScheduledAt()
    {
        return scheduledAt;
    }

    public void cancel()
    {
        if ( getFuture() != null )
        {
            getFuture().cancel( true );
        }

        setTaskState( TaskState.CANCELLED );

        getScheduler().removeFromTasksMap( this );
    }

    public void reset()
    {
        if ( getFuture() != null )
        {
            getFuture().cancel( false );
        }

        setTaskState( TaskState.SUBMITTED );

        setFuture( reschedule() );
    }

    public Throwable getBrokenCause()
    {
        return throwable;
    }

    public T get()
        throws ExecutionException,
            InterruptedException
    {
        return getFuture().get();
    }

    public T getIfDone()
    {
        if ( TaskState.FINISHED.equals( getTaskState() ) )
        {
            try
            {
                return getFuture().get();
            }
            catch ( ExecutionException e )
            {
                return null;
            }
            catch ( InterruptedException e )
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    protected void setLastRun( Date lastRun )
    {
        this.lastRun = new Date( lastRun.getTime() + 20 );
    }

    protected Future<T> reschedule()
    {
        if ( !isManualRunScheduled() )
        {
            SchedulerIterator iter = getScheduleIterator();

            if ( iter != null && !iter.isFinished() )
            {
                nextRun = iter.next();

                long nextTime = 0;

                if ( nextRun != null )
                {
                    nextTime = nextRun.getTime();
                }
                else
                {
                    nextTime = System.currentTimeMillis();
                }

                getScheduler().taskRescheduled( this );

                return getScheduler().getScheduledExecutorService().schedule(
                    this,
                    nextTime - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS );
            }
            else
            {
                nextRun = null;

                return null;
            }
        }
        else
        {
            nextRun = null;
            
            return null;
        }
    }

    public void runNow()
    {
        // if we are not RUNNING
        if ( !TaskState.RUNNING.equals( getTaskState() ) && !manualRun )
        {
            manualRun = true;

            getScheduler().getScheduledExecutorService().schedule( this, 0, TimeUnit.MILLISECONDS );
        }
    }

    public T call()
        throws Exception
    {
        T result = null;

        if ( SchedulerTask.class.isAssignableFrom( getCallable().getClass() ) )
        {
            // check for execution
            if ( !( (SchedulerTask<?>) getCallable() ).allowConcurrentExecution( getScheduler().getActiveTasks() ) )
            {
                // simply reschedule itself for 10sec
                nextRun = new Date( nextRun.getTime() + 10000 );
                
                setFuture( getScheduler().getScheduledExecutorService().schedule( this, 10000, TimeUnit.MILLISECONDS ) );
                
                setTaskState( TaskState.SLEEPING );

                return result;
            };
        }
        
        Future<T> nextFuture = null;

        if ( ( isEnabled() || manualRun ) && getTaskState().isActiveOrSubmitted()) 
        {
            setTaskState( TaskState.RUNNING );

            Date startDate = new Date();

            try
            {
                // Note that we need to do this prior to starting, so that the next run time will be updated properly
                // Rather than having to wait for the task to finish
                
                // If manually running, just grab the previous future and use that or create a new one
                if ( manualRun )
                {
                    nextFuture = getFuture();

                    manualRun = false;
                }
                // Otherwise, grab the next one
                else
                {
                    nextFuture = reschedule();
                }
                
                result = getCallable().call();

                if ( result != null )
                {
                    results.add( result );
                }
            }
            catch ( Throwable e )
            {
                manualRun = false;
                
                setBrokenCause( e );

                setTaskState( TaskState.BROKEN );

                if ( Exception.class.isAssignableFrom( e.getClass() ) )
                {
                    // this is an exception, pass it further
                    throw (Exception) e;
                }
                else
                {
                    // this is a Throwable or Error instance, pack it into an exception and rethrow
                    throw new TaskExecutionException( e );
                }
            }
            finally
            {
                setLastRun( startDate );
            }
        }

        // If manually running or having future, park this task to waiting        
        if ( isManualRunScheduled() )
        {
            setTaskState( TaskState.SUBMITTED );
        }
        else if ( nextFuture != null )
        {
            setTaskState( TaskState.WAITING );

            setFuture( nextFuture );
        }
        else
        {
            setTaskState( TaskState.FINISHED );

            getScheduler().removeFromTasksMap( this );
        }

        return result;
    }

    // IteratingTask

    public Date getLastRun()
    {
        return lastRun;
    }

    public Date getNextRun()
    {
        return nextRun;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }

    public List<T> getResults()
    {
        return results;
    }

    // ScheduledTask

    public Schedule getSchedule()
    {
        return schedule;
    }

    public void setSchedule( Schedule schedule )
    {
        this.schedule = schedule;

        this.scheduleIterator = null;
    }

    public SchedulerIterator getScheduleIterator()
    {
        if ( scheduleIterator == null && getSchedule() != null )
        {
            scheduleIterator = getSchedule().getIterator();
        }

        return scheduleIterator;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public Map<String, String> getTaskParams()
    {
        if ( taskParams == null )
        {
            taskParams = new HashMap<String, String>();
        }
        return taskParams;
    }
}
