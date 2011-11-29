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
package org.sonatype.nexus.plugins.capabilities.internal;

import static java.lang.String.format;
import static org.sonatype.appcontext.internal.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfiguration;
import org.sonatype.nexus.plugins.capabilities.support.activation.Conditions;
import com.google.common.base.Preconditions;

/**
 * Default {@link CapabilityRegistry} implementation.
 */
@Singleton
@Named
class DefaultCapabilityRegistry
    extends AbstractLoggingComponent
    implements CapabilityRegistry
{

    private final Map<String, CapabilityFactory> factories;

    private final ActivationContext activationContext;

    private final CapabilityConfiguration configuration;

    private final Conditions conditions;

    private final Map<String, CapabilityReference> references;

    private final Set<Listener> listeners;

    private final ReentrantReadWriteLock lock;

    @Inject
    DefaultCapabilityRegistry( final Map<String, CapabilityFactory> factories,
                               final ActivationContext activationContext,
                               final CapabilityConfiguration configuration,
                               final Conditions conditions )
    {
        this.activationContext = checkNotNull( activationContext );
        this.factories = checkNotNull( factories );
        this.configuration = Preconditions.checkNotNull( configuration );
        this.conditions = Preconditions.checkNotNull( conditions );

        references = new HashMap<String, CapabilityReference>();
        listeners = new HashSet<Listener>();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public CapabilityReference create( final String capabilityId, final String capabilityType )
    {
        assert capabilityId != null : "Capability id cannot be null";

        try
        {
            lock.writeLock().lock();

            final CapabilityFactory factory = factories.get( capabilityType );
            if ( factory == null )
            {
                throw new RuntimeException( format( "No factory found for a capability of type %s", capabilityType ) );
            }

            final Capability capability = factory.create( capabilityId );

            final CapabilityReference reference = createReference( capability );

            references.put( capabilityId, reference );

            getLogger().debug( "Created capability '{}'", capability );

            notify( reference, new Notifier( "added" )
            {
                @Override
                void run( final Listener listener, final CapabilityReference reference )
                {
                    listener.onAdd( reference );
                }
            } );

            return reference;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    @Override
    public CapabilityReference remove( final String capabilityId )
    {
        try
        {
            lock.writeLock().lock();

            final CapabilityReference reference = references.remove( capabilityId );
            if ( reference != null )
            {
                getLogger().debug( "Removed capability '{}'", reference.capability() );
                notify( reference, new Notifier( "removed" )
                {
                    @Override
                    void run( final Listener listener, final CapabilityReference reference )
                    {
                        listener.onRemove( reference );
                    }
                } );
            }
            return reference;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    @Override
    public CapabilityReference get( final String capabilityId )
    {
        try
        {
            lock.readLock().lock();

            return references.get( capabilityId );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<CapabilityReference> getAll()
    {
        try
        {
            lock.readLock().lock();

            return references.values();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public DefaultCapabilityRegistry addListener( final Listener listener )
    {
        try
        {
            lock.writeLock().lock();

            listeners.add( checkNotNull( listener ) );
            getLogger().debug( "Added listener '{}'. Notifying it about existing capabilities...", listener );
            for ( final CapabilityReference reference : references.values() )
            {
                try
                {
                    listener.onAdd( reference );
                }
                catch ( Exception e )
                {
                    getLogger().warn(
                        "Catched exception while notifying '{}' about existing capability '{}'",
                        new Object[]{ listener, reference.capability(), e }
                    );
                }
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }

        return this;
    }

    @Override
    public DefaultCapabilityRegistry removeListener( final Listener listener )
    {
        try
        {
            lock.writeLock().lock();

            listeners.remove( checkNotNull( listener ) );
            getLogger().debug( "Removed listener '{}'", listener );
        }
        finally
        {
            lock.writeLock().unlock();
        }

        return this;
    }

    void notify( final CapabilityReference reference, final Notifier notifier )
    {
        try
        {
            getLogger().debug( "Notifying {} capability registry listeners...", listeners.size() );
            lock.readLock().lock();

            for ( final Listener listener : listeners )
            {
                getLogger().debug(
                    "Notifying '{}' about {} capability '{}'",
                    new Object[]{ listener, notifier.description, reference.capability() }
                );
                try
                {
                    notifier.run( listener, reference );
                }
                catch ( Exception e )
                {
                    getLogger().warn(
                        "Catched exception while notifying '{}' about {} capability '{}'",
                        new Object[]{ listener, notifier.description, reference.capability(), e }
                    );
                }
            }
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    // @TestAccessible
    CapabilityReference createReference( final Capability capability )
    {
        return new DefaultCapabilityReference(
            this, activationContext, configuration, conditions, capability
        );
    }

    abstract static class Notifier
    {

        private String description;

        Notifier( final String description )
        {
            this.description = description;
        }

        abstract void run( Listener listener, CapabilityReference reference );
    }

}
