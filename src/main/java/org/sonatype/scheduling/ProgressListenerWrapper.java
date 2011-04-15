package org.sonatype.scheduling;

public class ProgressListenerWrapper
    implements ProgressListener
{
    public static final ProgressListenerWrapper DEVNULL = new ProgressListenerWrapper( null );

    private final ProgressListener wrapped;

    public ProgressListenerWrapper( final ProgressListener wrapped )
    {
        this.wrapped = wrapped;
    }

    public void beginTask( String name, int toDo )
    {
        if ( wrapped != null )
        {
            wrapped.beginTask( name, toDo );
        }
    }

    public void working( int work )
    {
        if ( wrapped != null )
        {
            wrapped.working( work );
        }
    }

    public void working( String message, int work )
    {
        if ( wrapped != null )
        {
            wrapped.working( message, work );
        }
    }

    public void endTask( String message )
    {
        if ( wrapped != null )
        {
            wrapped.endTask( message );
        }
    }

    public boolean isCancelled()
    {
        if ( wrapped != null )
        {
            return wrapped.isCancelled();
        }
        else
        {
            return false;
        }
    }
}
