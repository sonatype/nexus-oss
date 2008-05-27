package org.sonatype.nexus.scheduling.util;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.scheduling.ScheduleIterator;
import org.sonatype.nexus.scheduling.ScheduledTask;
import org.sonatype.nexus.scheduling.SubmittedTask;

public class DefaultRunnableTask
    implements SubmittedTask, ScheduledTask, Runnable
{
    private final Runnable runnable;

    private final ScheduleIterator scheduleIterator;

    private final ScheduledThreadPoolExecutor executor;

    private Future<?> future;

    private Date lastRun;

    public DefaultRunnableTask( Runnable runnable, ScheduleIterator scheduleIterator,
        ScheduledThreadPoolExecutor executor )
    {
        super();

        this.runnable = runnable;

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

    public void run()
    {
        lastRun = new Date();

        runnable.run();

        reschedule();
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
