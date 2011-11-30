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
package org.sonatype.nexus.plugins.capabilities.internal.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.capabilities.internal.config.DefaultCapabilityConfiguration.asMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import com.google.common.eventbus.Subscribe;

/**
 * Reacts to capability configuration events.
 *
 * @since 1.10.0
 */
@Named
@Singleton
class CapabilityConfigurationEventsHandler
    implements NexusEventBus.LoadOnStart
{

    private final CapabilityRegistry registry;

    @Inject
    CapabilityConfigurationEventsHandler( final CapabilityRegistry registry )
    {
        this.registry = checkNotNull( registry );
    }

    @Subscribe
    public void handle( final CapabilityConfigurationEvent.Added event )
    {
        final CCapability capabilityConfig = event.getCapability();
        final CapabilityReference ref = registry.create( capabilityConfig.getId(), capabilityConfig.getTypeId() );
        ref.create( asMap( capabilityConfig.getProperties() ) );
        if ( capabilityConfig.isEnabled() )
        {
            ref.enable();
        }
    }

    @Subscribe
    public void handle( final CapabilityConfigurationEvent.Loaded event )
    {
        final CCapability capabilityConfig = event.getCapability();
        final CapabilityReference ref = registry.create( capabilityConfig.getId(), capabilityConfig.getTypeId() );
        ref.load( asMap( capabilityConfig.getProperties() ) );
        if ( capabilityConfig.isEnabled() )
        {
            ref.enable();
        }
    }

    @Subscribe
    public void handle( final CapabilityConfigurationEvent.Updated event )
    {
        final CCapability capabilityConfig = event.getCapability();
        final CCapability previousCapabilityConfig = event.getPreviousCapability();
        final CapabilityReference ref = registry.get( capabilityConfig.getId() );
        if ( ref != null )
        {
            if ( previousCapabilityConfig.isEnabled() && !capabilityConfig.isEnabled() )
            {
                ref.disable();
            }
            ref.update( asMap( capabilityConfig.getProperties() ), asMap( previousCapabilityConfig.getProperties() ) );
            if ( !previousCapabilityConfig.isEnabled() && capabilityConfig.isEnabled() )
            {
                ref.enable();
            }
        }
    }

    @Subscribe
    public void handle( final CapabilityConfigurationEvent.Removed event )
    {
        final CCapability capabilityConfig = event.getCapability();
        final CapabilityReference ref = registry.get( capabilityConfig.getId() );
        if ( ref != null )
        {
            ref.remove();
            registry.remove( capabilityConfig.getId() );
        }
    }

    @Override
    public String toString()
    {
        return "Watch capabilities configuration changes";
    }

}
