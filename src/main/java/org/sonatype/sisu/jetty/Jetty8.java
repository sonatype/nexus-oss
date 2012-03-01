package org.sonatype.sisu.jetty;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.jetty.server.Server;
import org.sonatype.appcontext.AppContext;
import org.sonatype.sisu.jetty.mangler.ContextAttributeSetterMangler;
import org.sonatype.sisu.jetty.mangler.ServerMangler;
import org.sonatype.sisu.jetty.util.JettyUtils;

/**
 * A simple component managing embedded Jetty life cycle, configuring Jetty using passed in jetty.xml File,
 * interpolating it with passed in context maps, if any. Usable in cases like UT/ITs, when you can lookup this component
 * and control the Jetty instance with it.
 * 
 * @author cstamas
 */
@Named
@Singleton
public class Jetty8
{
    private final Server server;

    private final File jettyXml;

    private final ClassLoader classloader;

    private final Map<?, ?>[] contexts;

    private final AppContext appContext;

    private volatile JettyWrapperThread serverThread;

    public Jetty8( final File jettyXml, final Map<?, ?>... contexts )
        throws IOException
    {
        this( jettyXml, Thread.currentThread().getContextClassLoader(), contexts );
    }

    public Jetty8( final File jettyXml, final ClassLoader classloader, final Map<?, ?>... contexts )
        throws IOException
    {
        this.jettyXml = jettyXml;
        this.classloader = classloader;
        this.contexts = contexts;
        this.server = new Server();
        this.appContext = JettyUtils.configureServer( this.server, this.jettyXml, this.contexts );
        mangleServer( new ContextAttributeSetterMangler( AppContext.APPCONTEXT_KEY, appContext ) );
    }

    public AppContext getAppContext()
    {
        return appContext;
    }

    public synchronized boolean isStarted()
    {
        return serverThread != null;
    }

    public synchronized boolean isRunning()
    {
        return serverThread != null && serverThread.isAlive() && server.isStarted();
    }

    public synchronized void startJetty()
        throws Exception
    {
        if ( isStarted() )
        {
            throw new IllegalStateException( "Jetty7 already started, stop if first before starting again!" );
        }

        serverThread = new JettyWrapperThread( server );
        serverThread.setContextClassLoader( classloader );
        serverThread.startJetty();
    }

    public synchronized void stopJetty()
        throws Exception
    {
        if ( !isStarted() )
        {
            throw new IllegalStateException( "Jetty7 not started, start if first before stopping!" );
        }

        serverThread.stopJetty();
        serverThread = null;
    }

    public synchronized <T> T mangleServer( final ServerMangler<T> mangler )
    {
        return mangler.mangle( server );
    }

    // ==

    public static class JettyWrapperThread
        extends Thread
    {
        private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger( 1 );

        private final Server server;

        private final CountDownLatch started;

        private final CountDownLatch stopped;

        private volatile Exception exception;

        public JettyWrapperThread( final Server server )
        {
            super( "jetty-main-thread-" + INSTANCE_COUNTER.getAndIncrement() );
            this.server = server;
            this.started = new CountDownLatch( 1 );
            this.stopped = new CountDownLatch( 1 );
        }

        @Override
        public void run()
        {
            try
            {
                try
                {
                    server.start();
                }
                catch ( Exception e )
                {
                    exception = e;
                }
                finally
                {
                    started.countDown();
                }

                server.join();
            }
            catch ( InterruptedException e )
            {
                // nothing
            }
            finally
            {
                stopped.countDown();
            }
        }

        public void startJetty()
            throws Exception
        {
            start();
            started.await();

            if ( exception != null )
            {
                throw exception;
            }
        }

        public void stopJetty()
            throws Exception
        {
            final ClassLoader original = Thread.currentThread().getContextClassLoader();

            try
            {
                Thread.currentThread().setContextClassLoader( getContextClassLoader() );
                server.stop();
                stopped.await();

                if ( exception != null )
                {
                    throw exception;
                }
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( original );
            }
        }
    }
}
