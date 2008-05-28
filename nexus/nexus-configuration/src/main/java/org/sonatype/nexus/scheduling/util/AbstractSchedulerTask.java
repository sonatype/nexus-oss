package org.sonatype.nexus.scheduling.util;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.sonatype.nexus.scheduling.ScheduleIterator;
import org.sonatype.nexus.scheduling.ScheduledTask;
import org.sonatype.nexus.scheduling.SubmittedTask;
import org.sonatype.nexus.scheduling.TaskState;

public abstract class AbstractSchedulerTask<T>
    implements SubmittedTask, ScheduledTask
{
    private final ScheduleIterator scheduleIterator;

    private final ScheduledThreadPoolExecutor executor;

    private boolean enabled;

    private TaskState taskState;

    private Future<T> future;

    private Date lastRun;

    public AbstractSchedulerTask( ScheduleIterator scheduleIterator, ScheduledThreadPoolExecutor executor )
    {
        super();

        this.scheduleIterator = scheduleIterator;

        this.executor = executor;

        this.enabled = true;

        this.taskState = TaskState.SCHEDULED;
    }

    public void start()
    {
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

    protected Date getLastRun()
    {
        return lastRun;
    }

    protected void setLastRun( Date lastRun )
    {
        this.lastRun = lastRun;
    }

    protected ScheduleIterator getScheduleIterator()
    {
        return scheduleIterator;
    }

    protected ScheduledThreadPoolExecutor getExecutor()
    {
        return executor;
    }

    protected void setTaskState( TaskState state )
    {
        if ( !getTaskState().isEndingState() )
        {
            this.taskState = state;
        }
    }

    protected abstract Future<T> reschedule();

    // SubmittedTask

    public TaskState getTaskState()
    {
        return taskState;
    }

    public boolean isDone()
    {
        return getTaskState().isEndingState();
    }

    public void cancel()
    {
        getFuture().cancel( true );

        setTaskState( TaskState.CANCELLED );
    }

    // ScheduledTask

    public Date lastRun()
    {
        return getLastRun();
    }

    public Date nextRun()
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

}
