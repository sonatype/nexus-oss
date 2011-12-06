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
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistryEvent;
import com.google.common.annotations.VisibleForTesting;

/**
 * Default {@link CapabilityRegistry} implementation.
 */
@Singleton
@Named
class DefaultCapabilityRegistry
    extends AbstractLoggingComponent
    implements CapabilityRegistry
{

    private final NexusEventBus eventBus;

    private final ActivationConditionHandlerFactory activationConditionHandlerFactory;

    private final ValidityConditionHandlerFactory validityConditionHandlerFactory;

    private final Map<String, CapabilityFactory> capabilityFactories;

    private final Map<String, CapabilityReference> references;

    private final ReentrantReadWriteLock lock;

    @Inject
    DefaultCapabilityRegistry( final Map<String, CapabilityFactory> capabilityFactories,
                               final NexusEventBus eventBus,
                               final ActivationConditionHandlerFactory activationConditionHandlerFactory,
                               final ValidityConditionHandlerFactory validityConditionHandlerFactory )
    {
        this.eventBus = checkNotNull( eventBus );
        this.capabilityFactories = checkNotNull( capabilityFactories );
        this.activationConditionHandlerFactory = checkNotNull( activationConditionHandlerFactory );
        this.validityConditionHandlerFactory = checkNotNull( validityConditionHandlerFactory );

        references = new HashMap<String, CapabilityReference>();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public CapabilityReference create( final String capabilityId, final String capabilityType )
    {
        assert capabilityId != null : "Capability id cannot be null";

        try
        {
            lock.writeLock().lock();

            final CapabilityFactory factory = capabilityFactories.get( capabilityType );
            if ( factory == null )
            {
                throw new RuntimeException( format( "No factory found for a capability of type %s", capabilityType ) );
            }

            final Capability capability = factory.create( capabilityId );

            final CapabilityReference reference = createReference( capability );

            references.put( capabilityId, reference );

            getLogger().debug( "Created capability '{}'", capability );

            eventBus.post( new CapabilityRegistryEvent.Created( reference ) );

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
                eventBus.post( new CapabilityRegistryEvent.Removed( reference ) );
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

    @VisibleForTesting
    CapabilityReference createReference( final Capability capability )
    {
        return new DefaultCapabilityReference(
            eventBus, activationConditionHandlerFactory, validityConditionHandlerFactory, capability
        );
    }

}
