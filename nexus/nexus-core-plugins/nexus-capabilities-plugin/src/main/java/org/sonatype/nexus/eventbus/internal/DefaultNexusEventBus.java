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
package org.sonatype.nexus.eventbus.internal;

import java.util.concurrent.ConcurrentLinkedQueue;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.eventbus.internal.guava.EventBus;
import org.sonatype.nexus.eventbus.internal.guava.EventHandler;
import org.sonatype.nexus.logging.AbstractLoggingComponent;

/**
 * {@link NexusEventBus} implementation using guava event bus.
 *
 * It differs from guava event bus by dispatching events as they appear (is re-entrant). Guava will queue up all event
 * and dispatch them in the order they were posted, without re-entrance.
 *
 * @since 1.10.0
 */
@Named
@Singleton
class DefaultNexusEventBus
    extends AbstractLoggingComponent
    implements NexusEventBus
{

    private EventBus eventBus;

    @Inject
    DefaultNexusEventBus()
    {
        eventBus = new EventBus( "nexus" )
        {
            /** queues of events for the current thread to dispatch */
            private final ThreadLocal<ConcurrentLinkedQueue<EventWithHandler>> eventsToDispatch =
                new ThreadLocal<ConcurrentLinkedQueue<EventWithHandler>>()
                {
                    @Override
                    protected ConcurrentLinkedQueue<EventWithHandler> initialValue()
                    {
                        return new ConcurrentLinkedQueue<EventWithHandler>();
                    }
                };

            @Override
            protected void enqueueEvent( final Object event, final EventHandler handler )
            {
                eventsToDispatch.get().offer( new EventWithHandler( event, handler ) );
            }

            @Override
            protected void dispatchQueuedEvents()
            {
                while ( true )
                {
                    EventWithHandler eventWithHandler = eventsToDispatch.get().poll();
                    if ( eventWithHandler == null )
                    {
                        break;
                    }

                    dispatch( eventWithHandler.event, eventWithHandler.handler );
                }
            }
        };
    }

    @Override
    public NexusEventBus register( final Object handler )
    {
        eventBus.register( handler );
        getLogger().debug( "Registered handler '{}'", handler );
        return this;
    }

    @Override
    public NexusEventBus unregister( final Object handler )
    {
        eventBus.unregister( handler );
        getLogger().debug( "Unregistered handler '{}'", handler );
        return this;
    }

    @Override
    public NexusEventBus post( final Object event )
    {
        getLogger().debug( "Event '{}' fired", event );
        eventBus.post( event );
        return this;
    }

}
