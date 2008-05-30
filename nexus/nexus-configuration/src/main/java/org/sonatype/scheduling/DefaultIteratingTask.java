package org.sonatype.scheduling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.sonatype.scheduling.iterators.SchedulerIterator;

public class DefaultIteratingTask<T>
    extends DefaultSubmittedTask<T>
    implements IteratingTask<T>
{
    private final SchedulerIterator scheduleIterator;

    private boolean enabled;

    private Date lastRun;

    private List<T> results;

    public DefaultIteratingTask( String clazz, DefaultScheduler scheduler, Callable<T> callable,
        SchedulerIterator scheduleIterator )
    {
        super( clazz, scheduler, callable );

        this.scheduleIterator = scheduleIterator;

        this.enabled = true;

        this.results = new ArrayList<T>();
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

    public SchedulerIterator getScheduleIterator()
    {
        return scheduleIterator;
    }

    public List<T> getResults()
    {
        return results;
    }

}
