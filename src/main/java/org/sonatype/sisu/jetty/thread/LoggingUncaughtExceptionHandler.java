package org.sonatype.sisu.jetty.thread;

import java.lang.Thread.UncaughtExceptionHandler;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * A simple {@link UncaughtExceptionHandler} that will try to use Jetty log to dump out a warning log that something
 * nasty happened, to have at least some trail where the hell did the Thread went. Note: experimenting with OOMs showed,
 * that in case uncaught one is OOM, this handler might not even be invoked! But still, we tried our best at least.
 * 
 * @author cstamas
 * @since 1.3
 */
public class LoggingUncaughtExceptionHandler
    implements UncaughtExceptionHandler
{
    private static final Logger LOG = Log.getLogger( LoggingUncaughtExceptionHandler.class );

    public void uncaughtException( final Thread t, final Throwable e )
    {
        // try first a feedback with minimal impact, to have something in logs at least
        LOG.warn( "Jetty pooled thread got unexpected exception and is removed from thread pool. This might lead to system instability, system restart might be needed!" );

        // now try whatever (full log), but if OOMed, this might fail totally
        LOG.warn(
            "Thread \""
                + t.getName()
                + "\" got unexpected exception, will be removed from pool! Warning: if reason is OutOfMemoryError, it might lead to JVM instability in general, and is recommended to restart it!",
            e );
    }
}
