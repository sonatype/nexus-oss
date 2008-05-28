package org.sonatype.nexus.scheduling.util;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.scheduling.ScheduleIterator;
import org.sonatype.nexus.scheduling.ScheduledTask;
import org.sonatype.nexus.scheduling.SubmittedTask;
import org.sonatype.nexus.scheduling.TaskState;

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

    public void run()
    {
        if ( isEnabled() && getTaskState().isActive() )
        {
            setTaskState( TaskState.RUNNING );

            setLastRun( new Date() );

            try
            {
                runnable.run();
            }
            catch ( RuntimeException e )
            {
                setTaskState( TaskState.BROKEN );

                throw e;
            }
        }

        Future<Object> nextFuture = reschedule();

        if ( nextFuture != null )
        {
            setTaskState( TaskState.WAITING );

            setFuture( nextFuture );
        }
        else
        {
            setTaskState( TaskState.FINISHED );
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
