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
package org.sonatype.nexus.eventbus;

/**
 * Dispatches events to listeners, and provides ways for listeners to register
 * themselves.
 * <p/>
 * <p>The EventBus allows publish-subscribe-style communication between
 * components without requiring the components to explicitly register with one
 * another (and thus be aware of each other).  It is designed exclusively to
 * replace traditional Java in-process event distribution using explicit
 * registration. It is <em>not</em> a general-purpose publish-subscribe system,
 * nor is it intended for interprocess communication.
 * <p/>
 * <h2>Receiving Events</h2>
 * To receive events, an object should:<ol>
 * <li>Expose a public method, known as the <i>event handler</i>, which accepts
 * a single argument of the type of event desired;</li>
 * <li>Mark it with a {@link com.google.common.eventbus.Subscribe} annotation;</li>
 * <li>Pass itself to an EventBus instance's {@link #register(Object)} method.
 * </li>
 * </ol>
 * <p/>
 * <h2>Posting Events</h2>
 * To post an event, simply provide the event object to the
 * {@link #post(Object)} method.  The EventBus instance will determine the type
 * of event and route it to all registered listeners.
 * <p/>
 * <p>Events are routed based on their type &mdash; an event will be delivered
 * to any handler for any type to which the event is <em>assignable.</em>  This
 * includes implemented interfaces, all superclasses, and all interfaces
 * implemented by superclasses.
 * <p/>
 * <p>When {@code post} is called, all registered handlers for an event are run
 * in sequence, so handlers should be reasonably quick.  If an event may trigger
 * an extended process (such as a database load), spawn a thread or queue it for
 * later.  (For a convenient way to do this, use an {@link org.sonatype.nexus.eventbus.internal.guava.AsyncEventBus}.)
 * <p/>
 * <h2>Handler Methods</h2>
 * Event handler methods must accept only one argument: the event.
 * <p/>
 * <p>Handlers should not, in general, throw.  If they do, the EventBus will
 * catch and log the exception.  This is rarely the right solution for error
 * handling and should not be relied upon; it is intended solely to help find
 * problems during development.
 * <p/>
 * <p>The EventBus guarantees that it will not call a handler method from
 * multiple threads simultaneously, unless the method explicitly allows it by
 * bearing the {@link com.google.common.eventbus.AllowConcurrentEvents} annotation.  If this annotation is
 * not present, handler methods need not worry about being reentrant, unless
 * also called from outside the EventBus.
 * <p/>
 * <h2>Dead Events</h2>
 * If an event is posted, but no registered handlers can accept it, it is
 * considered "dead."  To give the system a second chance to handle dead events,
 * they are wrapped in an instance of {@link org.sonatype.nexus.eventbus.internal.guava.DeadEvent} and reposted.
 * <p/>
 * <p>If a handler for a supertype of all events (such as Object) is registered,
 * no event will ever be considered dead, and no DeadEvents will be generated.
 * Accordingly, while DeadEvent extends {@link Object}, a handler registered to
 * receive any Object will never receive a DeadEvent.
 * <p/>
 * <p>This class is safe for concurrent use.
 *
 * @since 1.10.0
 */
public interface NexusEventBus
{

    /**
     * Registers an event handler with this event bus.
     *
     * @param handler to be registered
     * @return itself, for fluent api usage
     */
    NexusEventBus register( Object handler );

    /**
     * Unregisters an event handler from this event bus.
     *
     * @param handler to be registered
     * @return itself, for fluent api usage
     */
    NexusEventBus unregister( Object handler );

    /**
     * Posts an event. Event bus will notify all previously registered handlers about this event.
     *
     * @param event an event
     * @return itself, for fluent api usage
     */
    NexusEventBus post( Object event );

    /**
     * Marker interface for all handlers that should be automatically registered with event bus when event bus is
     * created.
     */
    public interface LoadOnStart
    {

    }

}
