/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.events;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.slf4j.Logger;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.threads.NexusThreadFactory;
import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.plexus.appevents.Event;

/**
 * A default implementation of EventInspectorHost, a component simply collecting all EventInspectors and re-emitting
 * events towards them in they wants to receive it. For ones implementing {@link AsynchronousEventInspector} a cached
 * thread pool is used to execute them in a separate thread. In case of pool saturation, the caller thread will execute
 * the async inspector (as if it would be non-async one). Host cannot assume and does not know which inspector is
 * "less important" (could be dropped without having data loss in case of excessive load for example), hence it applies
 * same rules to all inspectors.
 * 
 * @author cstamas
 */
@Component( role = EventInspectorHost.class )
public class DefaultEventInspectorHost
    extends AbstractLoggingComponent
    implements EventInspectorHost, Disposable
{
    private final int HOST_THREAD_POOL_SIZE = SystemPropertiesHelper.getInteger(
        "org.sonatype.nexus.events.DefaultEventInspectorHost.poolSize", 500 );

    private final ThreadPoolExecutor hostThreadPool;

    @Requirement( role = EventInspector.class )
    private Map<String, EventInspector> eventInspectors;

    public DefaultEventInspectorHost()
    {
        // direct hand-off used! Host pool will use caller thread to execute async inspectors when pool full!
        this.hostThreadPool =
            new ThreadPoolExecutor( 0, HOST_THREAD_POOL_SIZE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                new NexusThreadFactory( "nxevthost", "Event Inspector Host" ), new CallerRunsPolicy() );
    }

    // == Disposable iface, to manage ExecutorService lifecycle

    public void dispose()
    {
        shutdown();
    }

    // == EventInspectorHost iface

    public void shutdown()
    {
        // we need clean shutdown, wait all background event inspectors to finish to have consistent state
        hostThreadPool.shutdown();
    }

    public boolean isCalmPeriod()
    {
        // "calm period" is when we have no queued nor active threads
        return hostThreadPool.getQueue().isEmpty() && hostThreadPool.getActiveCount() == 0;
    }

    // == EventListener iface

    public void onEvent( final Event<?> evt )
    {
        processEvent( evt );
    }

    // ==

    protected Set<EventInspector> getEventInspectors()
    {
        return new HashSet<EventInspector>( eventInspectors.values() );
    }

    protected void processEvent( final Event<?> evt )
    {
        final Set<EventInspector> inspectors = getEventInspectors();

        for ( EventInspector ei : inspectors )
        {
            try
            {
                if ( ei.accepts( evt ) )
                {
                    final EventInspectorHandler handler = new EventInspectorHandler( getLogger(), ei, evt );

                    if ( ei instanceof AsynchronousEventInspector && hostThreadPool != null
                        && !hostThreadPool.isShutdown() )
                    {
                        hostThreadPool.execute( handler );
                    }
                    else
                    {
                        handler.run();
                    }
                }
            }
            catch ( Exception e )
            {
                getLogger().warn(
                    "EventInspector implementation='" + ei.getClass().getName() + "' had problem accepting an event='"
                        + evt.getClass() + "'", e );
            }
        }
    }

    // ==

    public static class EventInspectorHandler
        implements Runnable
    {
        private final Logger logger;

        private final EventInspector ei;

        private final Event<?> evt;

        public EventInspectorHandler( final Logger logger, final EventInspector ei, final Event<?> evt )
        {
            this.logger = logger;
            this.ei = ei;
            this.evt = evt;
        }

        public void run()
        {
            try
            {
                ei.inspect( evt );
            }
            catch ( Exception e )
            {
                logger.warn( "EventInspector implementation='" + ei.getClass().getName()
                    + "' had problem inspecting an event='" + evt.getClass() + "'", e );
            }
        }
    }
}
