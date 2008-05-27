package org.sonatype.nexus.scheduling.util;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.scheduling.ScheduleIterator;
import org.sonatype.nexus.scheduling.ScheduledCallableTask;
import org.sonatype.nexus.scheduling.SubmittedCallableTask;

public class DefaultCallableTask<T>
    implements SubmittedCallableTask<T>, ScheduledCallableTask<T>, Callable<T>
{
    private final Callable<T> callable;

    private final ScheduleIterator scheduleIterator;

    private final ScheduledThreadPoolExecutor executor;

    private Future<T> future;

    private Date lastRun;

    public DefaultCallableTask( Callable<T> callable, ScheduleIterator scheduleIterator,
        ScheduledThreadPoolExecutor executor )
    {
        super();

        this.callable = callable;

        this.scheduleIterator = scheduleIterator;

        this.executor = executor;
    }

    public void start()
    {
        reschedule();
    }

    // SubmittedTask

    public void cancel()
    {
        future.cancel( true );
    }

    public boolean isCancelled()
    {
        return future.isCancelled();
    }

    public boolean isDone()
    {
        return future.isDone();
    }

    // ScheduledTask

    public Date lastRun()
    {
        return lastRun;
    }

    public Date nextRun()
    {
        if ( scheduleIterator != null )
        {
            return scheduleIterator.peekNext();
        }
        else
        {
            return null;
        }
    }

    public boolean isPaused()
    {
        if ( scheduleIterator != null )
        {
            return scheduleIterator.isPaused();
        }
        else
        {
            return false;
        }
    }

    public void setPaused( boolean paused )
    {
        if ( scheduleIterator != null )
        {
            scheduleIterator.setPaused( paused );
        }
    }

    // SubmittedCallableTask

    public T get()
        throws ExecutionException,
            InterruptedException
    {
        return future.get();
    }

    public T getIfDone()
    {
        if ( isDone() )
        {
            try
            {
                return future.get();
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

    public T call()
        throws Exception
    {
        lastRun = new Date();

        T result = callable.call();

        reschedule();

        return result;
    }

    protected void reschedule()
    {
        if ( scheduleIterator != null )
        {
            if ( !scheduleIterator.isFinished() )
            {
                future = executor.schedule(
                    this,
                    scheduleIterator.next().getTime() - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS );
            }
        }
        else if ( lastRun == null )
        {
            future = executor.submit( this );
        }
    }

}
