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
import static java.util.Collections.unmodifiableCollection;
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
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.CapabilityFactoryRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistryEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * Default {@link CapabilityRegistry} implementation.
 */
@Singleton
@Named
public class DefaultCapabilityRegistry
    extends AbstractLoggingComponent
    implements CapabilityRegistry
{

    private final NexusEventBus eventBus;

    private final ActivationConditionHandlerFactory activationConditionHandlerFactory;

    private final ValidityConditionHandlerFactory validityConditionHandlerFactory;

    private final CapabilityFactoryRegistry capabilityFactoryRegistry;

    private final Map<CapabilityIdentity, DefaultCapabilityReference> references;

    private final ReentrantReadWriteLock lock;

    @Inject
    DefaultCapabilityRegistry( final CapabilityFactoryRegistry capabilityFactoryRegistry,
                               final NexusEventBus eventBus,
                               final ActivationConditionHandlerFactory activationConditionHandlerFactory,
                               final ValidityConditionHandlerFactory validityConditionHandlerFactory )
    {
        this.eventBus = checkNotNull( eventBus );
        this.capabilityFactoryRegistry = checkNotNull( capabilityFactoryRegistry );
        this.activationConditionHandlerFactory = checkNotNull( activationConditionHandlerFactory );
        this.validityConditionHandlerFactory = checkNotNull( validityConditionHandlerFactory );

        references = new HashMap<CapabilityIdentity, DefaultCapabilityReference>();
        lock = new ReentrantReadWriteLock();
    }

    /**
     * Creates a capability given its id/type. if there is no capability available for specified type it will throw an
     * runtime exception.
     *
     * @param id   id of capability to be created
     * @param type type of capability to be created
     * @return created capability
     * @since 2.0
     */
    public DefaultCapabilityReference create( final CapabilityIdentity id, final CapabilityType type )
    {
        assert id != null : "Capability id cannot be null";

        try
        {
            lock.writeLock().lock();

            final CapabilityFactory factory = capabilityFactoryRegistry.get( type );
            if ( factory == null )
            {
                throw new RuntimeException( format( "No factory found for a capability of type %s", type ) );
            }

            final CapabilityContextProxy capabilityContextProxy = new CapabilityContextProxy(
                UninitializedCapabilityContext.INSTANCE
            );
            final Capability capability = factory.create( id, capabilityContextProxy );

            final DefaultCapabilityReference reference = createReference( type, capability, capabilityContextProxy );

            references.put( id, reference );

            getLogger().debug( "Created capability '{}'", capability );

            eventBus.post( new CapabilityRegistryEvent.Created( reference ) );

            return reference;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removed a capability from registry. If there is no capability with specified id in the registry it will pass
     * silently.
     *
     * @param id to remove
     * @return removed capability (if any), null otherwise
     * @since 2.0
     */
    public CapabilityReference remove( final CapabilityIdentity id )
    {
        try
        {
            lock.writeLock().lock();

            final CapabilityReference reference = references.remove( id );
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
    public DefaultCapabilityReference get( final CapabilityIdentity id )
    {
        try
        {
            lock.readLock().lock();

            return references.get( id );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<DefaultCapabilityReference> get( final Predicate<CapabilityReference> filter )
    {
        return unmodifiableCollection( Collections2.filter( getAll(), filter ) );
    }

    @Override
    public Collection<DefaultCapabilityReference> getAll()
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
    DefaultCapabilityReference createReference( final CapabilityType type,
                                                final Capability capability,
                                                final CapabilityContextProxy capabilityContextProxy )
    {
        return new DefaultCapabilityReference(
            eventBus,
            activationConditionHandlerFactory,
            validityConditionHandlerFactory,
            type,
            capability,
            capabilityContextProxy
        );
    }

}
