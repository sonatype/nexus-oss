package org.sonatype.scheduling;

public class ProgressListenerWrapper
    implements ProgressListener
{
    public static final ProgressListenerWrapper DEVNULL = new ProgressListenerWrapper( null );

    private final ProgressListener wrapped;

    private volatile boolean canceled;

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
            return canceled || wrapped.isCancelled();
        }
        else
        {
            return canceled;
        }
    }

    public void cancel()
    {
        canceled = true;
    }
}
