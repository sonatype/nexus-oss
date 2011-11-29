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

import com.google.common.eventbus.DeadEvent;

public interface NexusEventBus
{

    /**
     * Registers all handler methods on {@code object} to receive events.
     *
     * @param object object whose handler methods should be registered.
     * @return itself for fluent api usage
     */
    NexusEventBus register( Object object );

    /**
     * Unregisters all handler methods on a registered {@code object}.
     *
     * @param object object whose handler methods should be unregistered.
     * @return itself for fluent api usage
     * @throws IllegalArgumentException if the object was not previously registered.
     */
    NexusEventBus unregister( Object object );

    /**
     * Posts an event to all registered handlers.  This method will return
     * successfully after the event has been posted to all handlers, and
     * regardless of any exceptions thrown by handlers.
     * <p/>
     * <p>If no handlers have been subscribed for {@code event}'s class, and
     * {@code event} is not already a {@link DeadEvent}, it will be wrapped in a
     * DeadEvent and reposted.
     *
     * @param event event to post
     * @return itself for fluent api usage
     */
    NexusEventBus post( Object event );

    Latch lock( Class<?>... eventTypes );

    public interface Latch
    {

        void release();

    }

    public interface Handler
    {

    }

    public interface HandlerEvent
    {

        Object getHandler();

        Class<?> getEventType();

        void invokeHandler( Object event );

    }

    public interface HandlerRegisteredEvent
        extends HandlerEvent
    {

    }

    public interface HandlerUnregisteredEvent
        extends HandlerEvent
    {

    }

}
