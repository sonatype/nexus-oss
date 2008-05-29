package org.sonatype.scheduling;

import java.util.Date;
import java.util.concurrent.Future;

import org.sonatype.scheduling.iterators.ScheduleIterator;
import org.sonatype.scheduling.schedules.Schedule;

public abstract class AbstractSchedulerTask<T>
    implements SubmittedTask, ScheduledTask
{
    private final String clazz;

    private final ScheduleIterator scheduleIterator;

    private final DefaultScheduler scheduler;

    private final Schedule schedule;

    private boolean enabled;

    private TaskState taskState;

    private Date scheduledAt;

    private Future<T> future;

    private Date lastRun;

    public AbstractSchedulerTask( String clazz, ScheduleIterator scheduleIterator, DefaultScheduler scheduler,
        Schedule schedule )
    {
        super();

        this.clazz = clazz;

        this.scheduleIterator = scheduleIterator;

        this.scheduler = scheduler;

        this.schedule = schedule;

        this.enabled = true;

        this.taskState = TaskState.SCHEDULED;
    }

    public void start()
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

    protected void setLastRun( Date lastRun )
    {
        this.lastRun = lastRun;
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

    protected abstract Future<T> reschedule();

    // SubmittedTask

    public String getType()
    {
        return clazz;
    }

    public TaskState getTaskState()
    {
        return taskState;
    }

    public Date getScheduledAt()
    {
        return scheduledAt;
    }

    public Date getLastRun()
    {
        return lastRun;
    }

    public boolean isDone()
    {
        return getTaskState().isEndingState();
    }

    public void cancel()
    {
        getFuture().cancel( true );

        setTaskState( TaskState.CANCELLED );

        getScheduler().removeFromTasksMap( this );
    }

    // IteratingTask

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

    public ScheduleIterator getScheduleIterator()
    {
        return scheduleIterator;
    }
    
    // ScheduledTask

    public Schedule getSchedule()
    {
        return schedule;
    }


}
