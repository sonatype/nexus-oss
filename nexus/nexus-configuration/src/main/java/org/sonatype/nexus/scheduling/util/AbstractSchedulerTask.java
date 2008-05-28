package org.sonatype.nexus.scheduling.util;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.sonatype.nexus.scheduling.ScheduleIterator;
import org.sonatype.nexus.scheduling.ScheduledTask;
import org.sonatype.nexus.scheduling.SubmittedTask;

public abstract class AbstractSchedulerTask<T>
    implements SubmittedTask, ScheduledTask
{
    private final ScheduleIterator scheduleIterator;

    private final ScheduledThreadPoolExecutor executor;

    private boolean enabled;

    private Future<T> future;

    private Date lastRun;

    public AbstractSchedulerTask( ScheduleIterator scheduleIterator, ScheduledThreadPoolExecutor executor )
    {
        super();

        this.scheduleIterator = scheduleIterator;

        this.executor = executor;

        this.enabled = true;
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

    // SubmittedTask

    public void cancel()
    {
        getFuture().cancel( true );
    }

    public boolean isCancelled()
    {

        return getFuture().isCancelled();
    }

    public boolean isDone()
    {
        return getFuture().isDone();
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
