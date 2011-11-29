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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.eventbus.NexusEventBus;
import com.google.common.base.Throwables;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;

public class HandlerReflector
{

    private final EventBus eventBus;

    private Finder finder;

    public HandlerReflector( final EventBus eventBus )
    {
        this.eventBus = eventBus;
        finder = new Finder( eventBus );
    }

    public void publishHandlerRegisteredFor( final Object handler )
    {
        final Map<Class<?>, HandlerCallback> callbacks = finder.findAllHandlers( handler );
        if ( callbacks != null )
        {
            for ( final Map.Entry<Class<?>, HandlerCallback> entry : callbacks.entrySet() )
            {
                eventBus.post(
                    new DefaultHandlerRegisteredEvent( handler, entry.getKey() )
                    {
                        @Override
                        public void invokeHandler( final Object event )
                        {
                            entry.getValue().run( event );
                        }
                    }
                );
            }
        }
    }

    public void publishHandlerUnregisteredFor( final Object handler )
    {
        final Map<Class<?>, HandlerCallback> callbacks = finder.findAllHandlers( handler );
        if ( callbacks != null )
        {
            for ( final Map.Entry<Class<?>, HandlerCallback> entry : callbacks.entrySet() )
            {
                eventBus.post(
                    new DefaultHandlerUnregisteredEvent( handler, entry.getKey() )
                    {
                        @Override
                        public void invokeHandler( final Object event )
                        {
                            entry.getValue().run( event );
                        }
                    }
                );
            }
        }
    }

    abstract static class DefaultHandlerEvent
        implements NexusEventBus.HandlerEvent
    {

        public final Object handler;

        public final Class<?> eventType;

        private final String eventDescription;

        DefaultHandlerEvent( final Object handler,
                             final Class<?> eventType,
                             final String eventDescription )
        {
            this.handler = handler;
            this.eventType = eventType;
            this.eventDescription = eventDescription;
        }

        @Override
        public Object getHandler()
        {
            return handler;
        }

        @Override
        public Class<?> getEventType()
        {
            return eventType;
        }

        @Override
        public String toString()
        {
            return String.format( "Handler '%s' %s for %s ", handler, eventDescription, eventType.getName() );
        }

    }

    public abstract static class DefaultHandlerRegisteredEvent
        extends DefaultHandlerEvent
        implements NexusEventBus.HandlerRegisteredEvent
    {

        DefaultHandlerRegisteredEvent( final Object handler,
                                       final Class<?> eventType )
        {
            super( handler, eventType, "registered" );
        }

    }

    public abstract static class DefaultHandlerUnregisteredEvent
        extends DefaultHandlerEvent
        implements NexusEventBus.HandlerUnregisteredEvent
    {

        DefaultHandlerUnregisteredEvent( final Object handler,
                                         final Class<?> eventType )
        {
            super( handler, eventType, "unregistered" );
        }

    }

    private static class Finder
    {

        private final Method finderMethod;

        private final Object finder;

        private Finder( final EventBus eventBus )
        {
            try
            {
                final Field finderField = EventBus.class.getDeclaredField( "finder" );
                finderField.setAccessible( true );
                finder = finderField.get( eventBus );
                finderMethod = finder.getClass().getMethod( "findAllHandlers", Object.class );
                finderMethod.setAccessible( true );
            }
            catch ( Exception e )
            {
                throw Throwables.propagate( e );
            }
        }

        public Map<Class<?>, HandlerCallback> findAllHandlers( final Object handler )
        {
            Map<Class<?>, HandlerCallback> callbackMap = new HashMap<Class<?>, HandlerCallback>();
            try
            {
                final Multimap<Class<?>, ?> allHandlers = (Multimap<Class<?>, ?>) finderMethod.invoke(
                    finder, handler
                );
                for ( Map.Entry<Class<?>, ?> entry : allHandlers.entries() )
                {
                    final Method method = entry.getValue().getClass().getMethod( "handleEvent", Object.class );
                    method.setAccessible( true );
                    callbackMap.put( entry.getKey(), new HandlerCallback( entry.getValue(), method ) );
                }
            }
            catch ( Exception e )
            {
                throw Throwables.propagate( e );
            }
            return callbackMap;
        }
    }

    private static class HandlerCallback
    {

        private final Object handler;

        private final Method method;

        public HandlerCallback( final Object handler, final Method method )
        {
            this.handler = handler;
            this.method = method;
        }

        void run( final Object event )
        {
            try
            {
                method.invoke( handler, event );
            }
            catch ( Exception e )
            {
                throw Throwables.propagate( e );
            }
        }

        @Override
        public String toString()
        {
            return handler.toString();
        }
    }

}
