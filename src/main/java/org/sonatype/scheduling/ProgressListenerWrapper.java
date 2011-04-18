package org.sonatype.scheduling;

public class ProgressListenerWrapper
    implements ProgressListener
{
    private final ProgressListener wrapped;

    private volatile boolean cancelled;

    public ProgressListenerWrapper( final ProgressListener wrapped )
    {
        this.wrapped = wrapped;
    }

    public void beginTask( String name, int toDo )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.beginTask( name, toDo );
        }
    }

    public void working( int work )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.working( work );
        }
    }

    public void working( String message, int work )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.working( message, work );
        }
    }

    public void endTask( String message )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.endTask( message );
        }
    }

    public boolean isCancelled()
    {
        if ( wrapped != null )
        {
            return cancelled || wrapped.isCancelled();
        }
        else
        {
            return cancelled;
        }
    }

    public void cancel()
    {
        if ( wrapped != null )
        {
            wrapped.cancel();
        }

        cancelled = true;
    }
}
