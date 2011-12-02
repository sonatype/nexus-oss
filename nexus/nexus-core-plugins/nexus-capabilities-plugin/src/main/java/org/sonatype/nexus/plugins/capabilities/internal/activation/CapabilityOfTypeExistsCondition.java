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
package org.sonatype.nexus.plugins.capabilities.internal.activation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistryEvent;
import org.sonatype.nexus.plugins.capabilities.support.activation.AbstractCondition;
import com.google.common.eventbus.Subscribe;

/**
 * A condition that is satisfied when a capability of a specified type exists.
 *
 * @since 1.10.0
 */
public class CapabilityOfTypeExistsCondition
    extends AbstractCondition
{

    private final CapabilityRegistry capabilityRegistry;

    private final ReentrantReadWriteLock bindLock;

    final Class<?> type;

    public CapabilityOfTypeExistsCondition( final NexusEventBus eventBus,
                                            final CapabilityRegistry capabilityRegistry,
                                            final Class<? extends Capability> type )
    {
        super( eventBus );
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.type = type;
        bindLock = new ReentrantReadWriteLock();
    }

    @Override
    protected void doBind()
    {
        try
        {
            bindLock.writeLock().lock();
            for ( final CapabilityReference reference : capabilityRegistry.getAll() )
            {
                handle( new CapabilityRegistryEvent.Created( reference ) );
            }
        }
        finally
        {
            bindLock.writeLock().unlock();
        }
        getEventBus().register( this );
    }

    @Override
    public void doRelease()
    {
        getEventBus().unregister( this );
    }

    @Subscribe
    public void handle( final CapabilityRegistryEvent.Created event )
    {
        if ( !isSatisfied() && type.isAssignableFrom( event.getReference().capability().getClass() ) )
        {
            checkAllCapabilities();
        }
    }

    @Subscribe
    public void handle( final CapabilityRegistryEvent.Removed event )
    {
        if ( isSatisfied() && type.isAssignableFrom( event.getReference().capability().getClass() ) )
        {
            checkAllCapabilities();
        }
    }

    void checkAllCapabilities()
    {
        for ( final CapabilityReference ref : capabilityRegistry.getAll() )
        {
            if ( shouldEvaluateFor( ref ) )
            {
                setSatisfied( true );
                return;
            }
        }
        setSatisfied( false );
    }

    boolean shouldEvaluateFor( final CapabilityReference reference )
    {
        return type.isAssignableFrom( reference.capability().getClass() );
    }

    @Override
    protected void setSatisfied( final boolean satisfied )
    {
        try
        {
            bindLock.readLock().lock();
            super.setSatisfied( satisfied );
        }
        finally
        {
            bindLock.readLock().unlock();
        }
    }

    @Override
    public String toString()
    {
        return type.getSimpleName() + " exists";
    }

}
