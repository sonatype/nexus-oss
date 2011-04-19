package org.sonatype.scheduling;

public class TaskUtil
{
    private static final ThreadLocal<ProgressListener> CURRENT = new ThreadLocal<ProgressListener>()
    {
        protected ProgressListener initialValue()
        {
            return new ProgressListenerWrapper( null );
        }
    };

    protected static void setCurrent( ProgressListener progressListener )
    {
        CURRENT.set( new ProgressListenerWrapper( progressListener ) );
    }

    /**
     * Returns current {@link ProgressListener} instance, never returns null.
     * 
     * @return
     */
    public static ProgressListener getCurrentProgressListener()
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

        if ( getCurrentProgressListener().isCanceled() )
        {
            throw new TaskInterruptedException( "Thread \"" + Thread.currentThread().getName() + "\" is canceled!",
                true );
        }

        if ( Thread.interrupted() )
        {
            throw new TaskInterruptedException( "Thread \"" + Thread.currentThread().getName() + "\" is interrupted!",
                false );
        }
    }
}
