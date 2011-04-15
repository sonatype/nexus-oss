package org.sonatype.scheduling;

public class TaskUtil
{
    private static final ThreadLocal<ProgressListener> CURRENT = new ThreadLocal<ProgressListener>()
    {
        protected ProgressListener initialValue()
        {
            return ProgressListenerWrapper.DEVNULL;
        }
    };

    protected static void setCurrent( ProgressListener progressListener )
    {
        if ( progressListener == null )
        {
            CURRENT.set( ProgressListenerWrapper.DEVNULL );
        }
        else
        {
            CURRENT.set( progressListener );
        }
    }

    /**
     * Returns current {@link ProgressListener} instance, never returns null.
     * 
     * @return
     */
    public static ProgressListener getCurrent()
    {
        return CURRENT.get();
    }

    /**
     * Checks for user cancellation or thread interruption. In any of those both cases, {@link TaskInterruptedException}
     * is thrown that might be caught and handled by caller. If not handled, thread will die-off. If handled, caller
     * must ensure and handle interrupt flag of current thread.
     */
    public static void checkInterruption()
        throws TaskInterruptedException
    {
        Thread.yield();

        if ( getCurrent().isCancelled() )
        {
            throw new TaskInterruptedException( "Thread \"" + Thread.currentThread().getName() + "\" is cancelled!",
                true );
        }

        if ( Thread.currentThread().isInterrupted() )
        {
            throw new TaskInterruptedException( "Thread \"" + Thread.currentThread().getName() + "\" is interrupted!",
                false );
        }
    }
}
