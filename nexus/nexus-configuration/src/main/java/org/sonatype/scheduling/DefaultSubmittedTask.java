package org.sonatype.scheduling;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultSubmittedTask<T>
    implements SubmittedTask<T>, Callable<T>
{
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger( 0 );

    private final String id;

    private final String clazz;

    private final DefaultScheduler scheduler;

    private final Callable<T> callable;

    private TaskState taskState;

    private Date scheduledAt;

    private Future<T> future;

    private Exception exception;

    public DefaultSubmittedTask( String clazz, DefaultScheduler scheduler, Callable<T> callable )
    {
        super();

        this.id = String.valueOf( ID_GENERATOR.getAndIncrement() );

        this.clazz = clazz;

        this.scheduler = scheduler;
        
        this.callable = callable;

        this.taskState = TaskState.SUBMITTED;
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

    protected Future<T> reschedule()
    {
        return scheduler.getScheduledExecutorService().submit( this );
    }

    public T call()
        throws Exception
    {
        T result = null;

        if ( getTaskState().isActiveOrSubmitted() )
        {
            setTaskState( TaskState.RUNNING );

            try
            {
                result = callable.call();
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

        setTaskState( TaskState.FINISHED );

        getScheduler().removeFromTasksMap( this );

        return result;
    }

    // SubmittedTask

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
        getFuture().cancel( true );

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

}
