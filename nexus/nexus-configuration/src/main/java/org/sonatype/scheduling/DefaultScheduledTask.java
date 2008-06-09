package org.sonatype.scheduling;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.Schedule;

public class DefaultScheduledTask<T>
    implements ScheduledTask<T>, Callable<T>
{
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger( 0 );

    private final String id;
    
    private String name;

    private final String clazz;

    private final DefaultScheduler scheduler;

    private final Callable<T> callable;

    private TaskState taskState;

    private Date scheduledAt;

    private Future<T> future;

    private Exception exception;
    
    private boolean enabled;

    private Date lastRun;

    private List<T> results;
    
    private final Schedule schedule;
    
    private final SchedulerIterator scheduleIterator;
    
    private Map<String,String> taskParams;

    public DefaultScheduledTask( String name, String clazz, DefaultScheduler scheduler, Callable<T> callable, Schedule schedule, Map<String,String> taskParams )
    {
        super();
        
        this.id = String.valueOf( ID_GENERATOR.getAndIncrement() );
        
        this.name = name;

        this.clazz = clazz;

        this.scheduler = scheduler;
        
        this.callable = callable;

        this.taskState = TaskState.SUBMITTED;
        
        this.enabled = true;
        
        this.results = new ArrayList<T>();

        this.schedule = schedule;
        
        this.scheduleIterator = schedule.getIterator();
        
        this.taskParams = taskParams;
    }
    
    protected void start()
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

    protected void setBrokenCause( Exception e )
    {
        this.exception = e;
    }

    protected Callable<T> getCallable()
    {
        return callable;
    }

    public String getId()
    {
        return id;
    }

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

    public void cancel()
    {
        if ( getFuture() != null )
        {
            getFuture().cancel( true );
        }

        setTaskState( TaskState.CANCELLED );

        getScheduler().removeFromTasksMap( this );
    }

    public Exception getBrokenCause()
    {
        return exception;
    }

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
    
    public void setName( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
    
    public Map<String, String> getTaskParams()
    {
        if ( taskParams == null )
        {
            taskParams = new HashMap<String,String>();
        }
        return taskParams;
    }
}
