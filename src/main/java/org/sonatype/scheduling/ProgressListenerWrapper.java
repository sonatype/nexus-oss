package org.sonatype.scheduling;

public class ProgressListenerWrapper
    implements ProgressListener
{
    private final ProgressListener wrapped;

    public ProgressListenerWrapper( final ProgressListener wrapped )
    {
        this.wrapped = wrapped;
    }

    public void beginTask( final String name )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.beginTask( name );
        }
    }

    public void beginTask( final String name, final int toDo )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.beginTask( name, toDo );
        }
    }

    public void working( final int workDone )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.working( workDone );
        }
    }

    public void working( final String message )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.working( message );
        }
    }

    public void working( final String message, final int work )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.working( message, work );
        }
    }

    public void endTask( final String message )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.endTask( message );
        }
    }

    public boolean isCanceled()
    {
        if ( wrapped != null )
        {
            return wrapped.isCanceled();
        }
        else
        {
            return false;
        }
    }

    public void cancel()
    {
        if ( wrapped != null )
        {
            wrapped.cancel();
        }
    }
}
