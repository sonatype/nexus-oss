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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

/**
 * A default implementation of EventInspectorHost, a component simply collecting all EventInspectors and re-emitting
 * events towards them in they wants to receive it.
 * 
 * @author cstamas
 */
@Component( role = EventInspectorHost.class )
public class DefaultEventInspectorHost
    extends AbstractLogEnabled
    implements EventInspectorHost
{
    @Requirement( role = EventInspector.class )
    private Map<String, EventInspector> eventInspectors;

    private Executor executor = Executors.newCachedThreadPool();

    public void processEvent( Event<?> evt )
    {
        for ( Map.Entry<String, EventInspector> entry : eventInspectors.entrySet() )
        {
            EventInspector ei = entry.getValue();

            EventInspectorHandler handler = new EventInspectorHandler( getLogger(), ei, evt );

            if ( ei instanceof AsynchronousEventInspector )
            {
                executor.execute( handler );
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
