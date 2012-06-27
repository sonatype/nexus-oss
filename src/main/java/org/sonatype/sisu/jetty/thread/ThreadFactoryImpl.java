package org.sonatype.sisu.jetty.thread;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

/**
 * Simple thread factory implementation that puts Jetty pooled threads into it's own group and installs
 * {@link UncaughtExceptionHandler} to created threads.
 * 
 * @author cstamas
 * @since 1.3
 */
public class ThreadFactoryImpl
    implements ThreadFactory
{
    private final ThreadGroup group;

    private final UncaughtExceptionHandler uncaughtExceptionHandler;

    public ThreadFactoryImpl()
    {
        this( new ThreadGroup( "Jetty8" ), new LoggingUncaughtExceptionHandler() );
    }

    public ThreadFactoryImpl( final ThreadGroup group, final UncaughtExceptionHandler uncaughtExceptionHandler )
    {
        this.group = group;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    public Thread newThread( final Runnable runnable )
    {
        final Thread t = new Thread( group, runnable );
        t.setUncaughtExceptionHandler( uncaughtExceptionHandler );
        return t;
    }
}
