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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.eventbus.NexusEventBus;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;

@Named
@Singleton
class DefaultNexusEventBus
    implements NexusEventBus
{

    private Map<Class<?>, CountDownLatch> latches;

    private EventBus eventBus;

    private HandlerReflector finder;

    @Inject
    DefaultNexusEventBus()
    {
        eventBus = new EventBus( "nexus" );
        finder = new HandlerReflector( eventBus );
        latches = new HashMap<Class<?>, CountDownLatch>();
    }

    @Override
    public NexusEventBus register( final Object object )
    {
        finder.publishHandlerRegisteredFor( object );
        eventBus.register( object );
        return this;
    }

    @Override
    public NexusEventBus unregister( final Object object )
    {
        eventBus.unregister( object );
        finder.publishHandlerUnregisteredFor( object );
        return this;
    }

    @Override
    public NexusEventBus post( final Object event )
    {
        final CountDownLatch latch = latches.get( event.getClass() );
        if ( latch != null )
        {
            try
            {
                latch.await();
            }
            catch ( InterruptedException e )
            {
                throw Throwables.propagate( e );
            }
        }
        eventBus.post( event );
        return this;
    }

    @Override
    public Latch lock( final Class<?>... eventTypes )
    {
        final CountDownLatch latch = new CountDownLatch( 1 );
        for ( final Class<?> eventType : eventTypes )
        {
            latches.put( eventType, latch );
        }
        return new Latch()
        {
            @Override
            public void release()
            {
                for ( final Class<?> eventType : eventTypes )
                {
                    latches.remove( eventType );
                }
                latch.countDown();
            }
        };
    }

}
