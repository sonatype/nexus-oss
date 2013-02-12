package org.sonatype.nexus.util.task;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Collection of static methods allowing to use {@link ProgressListener} and track cancellation or interruption state in
 * less intrusive way than passing it deep into caller hierarchy.
 * 
 * @author cstamas
 * @since 2.4
 */
public class TaskUtil
{
    /**
     * Thread local variable holding the current {@link Cancelable} of the given (current) {@link Thread}.
     */
    private static final ThreadLocal<Cancelable> CURRENT = new ThreadLocal<Cancelable>()
    {
        @Override
        protected Cancelable initialValue()
        {
            return new CancelableSupport();
        }
    };

    /**
     * Static helper class, do not instantiate it.
     */
    private TaskUtil()
    {
        // no instances of this please
    }

    /**
     * Protected method that is meant to register current thread's {@link Cancelable} instance. See
     * {@link RunnableSupport}.
     * 
     * @param cancelable
     */
    protected static void setCurrentCancelable( final Cancelable cancelable )
    {
        if ( cancelable == null )
        {
            CURRENT.set( new CancelableSupport() );
        }
        else
        {
            CURRENT.set( cancelable );
        }
    }

    /**
     * Returns current {@link Cancelable} instance, never returns {@code null}.
     * 
     * @return the {@link Cancelable} instance, never {@code null}.
     */
    public static Cancelable getCurrentCancelable()
    {
        return CURRENT.get();
    }

    /**
     * Checks for user cancellation or thread interruption. It gets the current {@link Cancelable} using
     * {@link #getCurrentCancelable()} and uses {@link #checkInterruption(Cancelable)} method.
     * 
     * @throws TaskInterruptedException
     */
    public static void checkInterruption()
        throws TaskInterruptedException
    {
        checkInterruption( getCurrentCancelable() );
    }

    /**
     * Checks for user cancellation or thread interruption. In any of those both cases, {@link TaskInterruptedException}
     * is thrown that might be caught and handled by caller. If not handled, thread will die-off. If handled, caller
     * must ensure and handle interrupt flag of current thread.
     * 
     * @param c
     * @throws TaskInterruptedException
     */
    public static void checkInterruption( final Cancelable c )
        throws TaskInterruptedException
    {
        final Cancelable cancelable = checkNotNull( c );
        Thread.yield();
        if ( cancelable.isCanceled() )
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
