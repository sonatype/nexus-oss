package org.sonatype.scheduling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.Schedule;

public class DefaultScheduledTask<T>
    extends DefaultSubmittedTask<T>
    implements ScheduledTask<T>
{
    private boolean enabled;

    private Date lastRun;

    private List<T> results;
    
    private final Schedule schedule;
    
    private final SchedulerIterator scheduleIterator;

    public DefaultScheduledTask( String clazz, DefaultScheduler scheduler, Callable<T> callable, Schedule schedule )
    {
        super( clazz, scheduler, callable );
        
        this.enabled = true;
        
        this.results = new ArrayList<T>();

        this.schedule = schedule;
        
        this.scheduleIterator = schedule.getIterator();
    }
    
    protected void setLastRun( Date lastRun )
    {
        this.lastRun = lastRun;
    }

    protected Future<T> reschedule()
    {
        if ( !getScheduleIterator().isFinished() )
        {
            return getScheduler().getScheduledExecutorService().schedule(
                this,
                getScheduleIterator().next().getTime() - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS );
        }
        else
        {
            return null;
        }
    }

    public T call()
        throws Exception
    {
        T result = null;

        if ( isEnabled() && getTaskState().isActiveOrSubmitted() )
        {
            setTaskState( TaskState.RUNNING );

            setLastRun( new Date() );

            try
            {
                result = getCallable().call();

                results.add( result );
            }
            catch ( Exception e )
            {
                setBrokenCause( e );

                setTaskState( TaskState.BROKEN );

                // TODO: Should we remove brokeneds?
                // getScheduler().removeFromTasksMap( this );

                throw e;
            }
        }

        Future<T> nextFuture = reschedule();

        if ( nextFuture != null )
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
        if ( getScheduleIterator() != null )
        {
            return getScheduleIterator().peekNext();
        }
        else
        {
            return null;
        }
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
    
    public SchedulerIterator getScheduleIterator()
    {
        return scheduleIterator;
    }

}
