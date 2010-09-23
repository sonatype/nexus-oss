/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.events;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.threads.NexusThreadFactory;
import org.sonatype.plexus.appevents.Event;

/**
 * A default implementation of EventInspectorHost, a component simply collecting all EventInspectors and re-emitting
 * events towards them in they wants to receive it. TODO: count inspector exceptions, and stop using them after some
 * threshold (like 3 exceptions).
 * 
 * @author cstamas
 */
@Component( role = EventInspectorHost.class )
public class DefaultEventInspectorHost
    extends AbstractLogEnabled
    implements EventInspectorHost, Startable
{
    @Requirement( role = EventInspector.class )
    private Map<String, EventInspector> eventInspectors;

    private ExecutorService executor;

    // == Startable iface, to manage ExecutorService lifecycle

    public void start()
        throws StartingException
    {
        // set up executor
        executor = Executors.newCachedThreadPool( new NexusThreadFactory( "nxevthost", "Event Inspector Host" ) );
    }

    public void stop()
        throws StoppingException
    {
        shutdown();
    }

    // ==

    public void shutdown()
    {
        // we need clean shutdown, wait all bg event inspectors to finish to have consistent state
        executor.shutdown();
    }

    public boolean isCalmPeriod()
    {
        final ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;

        // "calm period" is when we have no active threads, neither queued ones
        return tpe.getActiveCount() == 0 && tpe.getQueue().isEmpty();
    }

    // ==

    public void processEvent( Event<?> evt )
    {
        for ( Map.Entry<String, EventInspector> entry : eventInspectors.entrySet() )
        {
            EventInspector ei = entry.getValue();

            EventInspectorHandler handler = new EventInspectorHandler( getLogger(), ei, evt );

            // NEXUS-3800: async execution
            // For now, turned off. Our core is happy and snappy with it, but some of our ITs are still unprepared for
            // this
            // since they do deploy-askIndexer and usually fail, since now indexer maintenance is async!
            // Commenting this out all puts back into "old state". Later, we should review ITs and reenable this.
            // ==

            // handler.run();

            // ==
            if ( ei instanceof AsynchronousEventInspector && executor != null && !executor.isShutdown() )
            {
                try
                {
                    executor.execute( handler );
                }
                catch ( RejectedExecutionException e )
                {
                    // execute it in sync mode, executor is either full or shutdown (?)
                    // in case executor is full, this "slowdown" will make it able consume and build up
                    handler.run();
                }
            }
            else
            {
                handler.run();
            }
        }
    }

    public void onEvent( Event<?> evt )
    {
        processEvent( evt );
    }

    // ==

    public static class EventInspectorHandler
        implements Runnable
    {
        private final Logger logger;

        private final EventInspector ei;

        private final Event<?> evt;

        public EventInspectorHandler( Logger logger, EventInspector ei, Event<?> evt )
        {
            super();
            this.logger = logger;
            this.ei = ei;
            this.evt = evt;
        }

        public void run()
        {
            try
            {
                if ( ei.accepts( evt ) )
                {
                    ei.inspect( evt );
                }
            }
            catch ( Throwable e )
            {
                logger.warn( "EventInspector implementation='" + ei.getClass().getName()
                    + "' had problem inspecting an event='" + evt.getClass() + "'", e );
            }
        }

    }
}
