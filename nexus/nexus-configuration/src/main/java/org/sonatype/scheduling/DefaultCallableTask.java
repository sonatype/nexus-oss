package org.sonatype.scheduling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.sonatype.scheduling.iterators.ScheduleIterator;
import org.sonatype.scheduling.schedules.Schedule;

public class DefaultCallableTask<T>
    extends AbstractSchedulerTask<T>
    implements ScheduledCallableTask<T>, Callable<T>
{
    private final Callable<T> callable;

    private List<T> results;

    public DefaultCallableTask( Class<?> clazz, Callable<T> callable, ScheduleIterator scheduleIterator,
        DefaultScheduler scheduler, Schedule schedule )
    {
        super( clazz, scheduleIterator, scheduler, schedule );

        this.callable = callable;

        this.results = new ArrayList<T>();
    }

    // SubmittedCallableTask

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

    // ScheduledCallableTask

    public T getLast()
        throws ExecutionException,
            InterruptedException
    {
        return get();
    }

    public T getLastIfDone()
    {
        return getIfDone();
    }

    public int getResultCount()
    {
        return results.size();
    }

    public T get( int i )
        throws IndexOutOfBoundsException
    {
        return results.get( i );
    }

    // Other

    public T call()
        throws Exception
    {
        T result = null;

        if ( isEnabled() && getTaskState().isActive() )
        {
            setTaskState( TaskState.RUNNING );

            setLastRun( new Date() );

            try
            {
                result = callable.call();

                results.add( result );
            }
            catch ( Exception e )
            {
                setTaskState( TaskState.BROKEN );

                getScheduler().removeFromTasksMap( this );

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

    protected Future<T> reschedule()
    {
        if ( getScheduleIterator() != null && !getScheduleIterator().isFinished() )
        {
            return getScheduler().getScheduledExecutorService().schedule(
                this,
                getScheduleIterator().next().getTime() - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS );
        }
        else if ( getLastRun() == null )
        {
            return getScheduler().getScheduledExecutorService().submit( this );
        }
        else
        {
            return null;
        }
    }

}
