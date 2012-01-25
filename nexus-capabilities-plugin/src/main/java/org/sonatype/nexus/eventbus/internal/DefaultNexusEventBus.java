/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.eventbus.internal;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.eventbus.internal.guava.EventBus;
import org.sonatype.nexus.eventbus.internal.guava.EventHandler;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import com.google.common.collect.Lists;

/**
 * {@link NexusEventBus} implementation using guava event bus.
 * <p/>
 * It differs from guava event bus by dispatching events as they appear (is re-entrant). Guava will queue up all event
 * and dispatch them in the order they were posted, without re-entrance.
 *
 * @since 2.0
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
            /** List of events for the current thread to dispatch */
            private final ThreadLocal<List<EventWithHandler>> eventsToDispatch =
                new ThreadLocal<List<EventWithHandler>>();

            @Override
            protected void enqueueEvent( final Object event, final EventHandler handler )
            {
                if ( eventsToDispatch.get() == null )
                {
                    eventsToDispatch.set( Lists.<EventWithHandler>newArrayList() );
                }
                eventsToDispatch.get().add( new EventWithHandler( event, handler ) );
            }

            @Override
            protected void dispatchQueuedEvents()
            {
                final List<EventWithHandler> eventWithHandlers = eventsToDispatch.get();
                if ( eventWithHandlers != null )
                {
                    eventsToDispatch.remove();
                    for ( final EventWithHandler eventWithHandler : eventWithHandlers )
                    {
                        dispatch( eventWithHandler.event, eventWithHandler.handler );
                    }
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
