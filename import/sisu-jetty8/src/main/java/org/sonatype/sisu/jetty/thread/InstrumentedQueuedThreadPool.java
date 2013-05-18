/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.sisu.jetty.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * An extension of Jetty's default {@link QueuedThreadPool}, that makes possible to plug-in custom {@link ThreadFactory}
 * that suits your needs (ie. to instrument or do anything else with the Threads being created for Jetty's pool). Only
 * one method is overridden, the {@link #newThread(Runnable)}, and all the constructors are kept too. Also,
 * {@link JettyWatchdog} daemon thread is started to nag if any "thread death" is detected.
 * <p>
 * To use this class in Server, you need following line(s) in your jetty.xml:
 * 
 * <pre>
 * &lt;Configure id="Server" class="org.eclipse.jetty.server.Server"&gt;
 *   &lt;Set name="threadPool"&gt;
 *     &lt;New class="org.sonatype.sisu.jetty.thread.InstrumentedQueuedThreadPool"/&gt;
 *   &lt;/Set&gt;
 *   &lt;Call name="addConnector"&gt;
 *   ...
 * </pre>
 * 
 * By default (if configured exactly as above), {@link ThreadFactoryImpl} will be used, and daemon thread running
 * {@link JettyWatchdog} will be started. The result is that Jetty's QTP threads will get their own thread group, all of
 * them will have {@link LoggingUncaughtExceptionHandler} installed, and system will nag you with WARN level logs
 * periodically (every 10 seconds) from the moment that "unexpected death(s)" of QTP threads are detected.
 * 
 * @author cstamas
 * @since 1.3
 */
public class InstrumentedQueuedThreadPool
    extends QueuedThreadPool
{
    private final ThreadFactory threadFactory;

    public InstrumentedQueuedThreadPool()
    {
        this( new ThreadFactoryImpl() );
    }

    public InstrumentedQueuedThreadPool( final BlockingQueue<Runnable> jobQ )
    {
        this( new ThreadFactoryImpl(), jobQ );
    }

    public InstrumentedQueuedThreadPool( final int maxThreads )
    {
        this( new ThreadFactoryImpl(), maxThreads );
    }

    public InstrumentedQueuedThreadPool( final ThreadFactory threadFactory )
    {
        super();
        this.threadFactory = validate( threadFactory );
        startWatchdog();
    }

    public InstrumentedQueuedThreadPool( final ThreadFactory threadFactory, final BlockingQueue<Runnable> jobQ )
    {
        super( jobQ );
        this.threadFactory = validate( threadFactory );
        startWatchdog();
    }

    public InstrumentedQueuedThreadPool( final ThreadFactory threadFactory, final int maxThreads )
    {
        super( maxThreads );
        this.threadFactory = validate( threadFactory );
        startWatchdog();
    }

    public ThreadFactory getThreadFactory()
    {
        return threadFactory;
    }

    protected ThreadFactory validate( final ThreadFactory threadFactory )
    {
        if ( threadFactory == null )
        {
            throw new NullPointerException( "ThreadFactory set on thread pool cannot be null!" );
        }
        return threadFactory;
    }

    protected void startWatchdog()
    {
        final Thread watchdogThread = new Thread( new JettyWatchdog(), "JettyQTPWatchdog" );
        watchdogThread.setDaemon( true );
        watchdogThread.setPriority( Thread.MIN_PRIORITY );
        watchdogThread.start();
    }

    @Override
    protected Thread newThread( final Runnable runnable )
    {
        return threadFactory.newThread( new RunnableWrapper( runnable ) );
    }
}
