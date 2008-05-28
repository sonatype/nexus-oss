package org.sonatype.nexus.scheduling.util;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.scheduling.ScheduleIterator;
import org.sonatype.nexus.scheduling.ScheduledTask;
import org.sonatype.nexus.scheduling.SubmittedTask;

public class DefaultRunnableTask
    extends AbstractSchedulerTask<Object>
    implements SubmittedTask, ScheduledTask, Runnable
{
    private final Runnable runnable;

    public DefaultRunnableTask( Runnable runnable, ScheduleIterator scheduleIterator,
        ScheduledThreadPoolExecutor executor )
    {
        super( scheduleIterator, executor );

        this.runnable = runnable;
    }

    public void start()
    {
        setFuture( reschedule() );
    }

    public void run()
    {
        if ( isEnabled() )
        {
            setLastRun( new Date() );

            runnable.run();
        }

        Future<Object> nextFuture = reschedule();

        if ( nextFuture != null )
        {
            setFuture( nextFuture );
        }
    }

    protected Future<Object> reschedule()
    {
        if ( getScheduleIterator() != null && !getScheduleIterator().isFinished() )
        {
            return (Future<Object>) getExecutor().schedule(
                this,
                getScheduleIterator().next().getTime() - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS );
        }
        else if ( getLastRun() == null )
        {
            return (Future<Object>) getExecutor().submit( this );
        }
        else
        {
            return null;
        }
    }
}
