package org.sonatype.sisu.jetty.thread;

/**
 * A simple wrapper that simply "detects" did a wrapped runnable "died" or exited cleanly. Once death detected, the flag
 * remains set.
 * 
 * @author cstamas
 * @since 1.3
 */
public class RunnableWrapper
    implements Runnable
{
    private final Runnable runnable;

    static boolean unexpectedThrowable = false;

    public RunnableWrapper( final Runnable runnable )
    {
        this.runnable = runnable;
    }

    public void run()
    {
        try
        {
            runnable.run();
        }
        catch ( Throwable e )
        {
            unexpectedThrowable = true;
        }
    }
}
