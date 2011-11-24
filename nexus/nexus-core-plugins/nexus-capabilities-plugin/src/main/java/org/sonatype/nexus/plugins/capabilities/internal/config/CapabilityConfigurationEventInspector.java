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

import static org.sonatype.nexus.plugins.capabilities.internal.config.DefaultCapabilityConfiguration.asMap;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.config.events.CapabilityConfigurationAddEvent;
import org.sonatype.nexus.plugins.capabilities.internal.config.events.CapabilityConfigurationEvent;
import org.sonatype.nexus.plugins.capabilities.internal.config.events.CapabilityConfigurationLoadEvent;
import org.sonatype.nexus.plugins.capabilities.internal.config.events.CapabilityConfigurationRemoveEvent;
import org.sonatype.nexus.plugins.capabilities.internal.config.events.CapabilityConfigurationUpdateEvent;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

@Singleton
public class CapabilityConfigurationEventInspector
    implements EventInspector
{

    private final CapabilityRegistry registry;

    @Inject
    public CapabilityConfigurationEventInspector( final CapabilityRegistry registry )
    {
        this.registry = registry;

    }

    public boolean accepts( final Event<?> evt )
    {
        return evt != null
            && evt instanceof CapabilityConfigurationEvent;
    }

    public void inspect( final Event<?> evt )
    {
        if ( !accepts( evt ) )
        {
            return;
        }
        if ( evt instanceof CapabilityConfigurationAddEvent )
        {
            handle( (CapabilityConfigurationAddEvent) evt );
        }
        else if ( evt instanceof CapabilityConfigurationLoadEvent )
        {
            handle( (CapabilityConfigurationLoadEvent) evt );
        }
        else if ( evt instanceof CapabilityConfigurationRemoveEvent )
        {
            handle( (CapabilityConfigurationRemoveEvent) evt );
        }
        else if ( evt instanceof CapabilityConfigurationUpdateEvent )
        {
            handle( (CapabilityConfigurationUpdateEvent) evt );
        }
    }

    private void handle( final CapabilityConfigurationAddEvent evt )
    {
        final CCapability capabilityConfig = evt.getCapability();
        final CapabilityReference ref = registry.create( capabilityConfig.getId(), capabilityConfig.getTypeId() );
        ref.capability().create( asMap( capabilityConfig.getProperties() ) );
        if ( capabilityConfig.isEnabled() )
        {
            ref.enable();
        }
    }

    private void handle( final CapabilityConfigurationLoadEvent evt )
    {
        final CCapability capabilityConfig = evt.getCapability();
        final CapabilityReference ref = registry.create( capabilityConfig.getId(), capabilityConfig.getTypeId() );
        ref.capability().load( asMap( capabilityConfig.getProperties() ) );
        if ( capabilityConfig.isEnabled() )
        {
            ref.enable();
        }
    }

    private void handle( final CapabilityConfigurationUpdateEvent evt )
    {
        final CCapability capabilityConfig = evt.getCapability();
        final CCapability previousCapabilityConfig = evt.getPreviousCapability();
        final CapabilityReference ref = registry.get( capabilityConfig.getId() );
        if ( ref != null )
        {
            if ( previousCapabilityConfig.isEnabled() && !capabilityConfig.isEnabled() )
            {
                ref.disable();
            }
            if ( !sameProperties( previousCapabilityConfig, capabilityConfig ) )
            {
                ref.capability().update( asMap( capabilityConfig.getProperties() ) );
            }
            if ( !previousCapabilityConfig.isEnabled() && capabilityConfig.isEnabled() )
            {
                ref.enable();
            }
        }
    }

    private void handle( final CapabilityConfigurationRemoveEvent evt )
    {
        final CCapability capabilityConfig = evt.getCapability();
        final CapabilityReference ref = registry.get( capabilityConfig.getId() );
        if ( ref != null )
        {
            registry.remove( ref.capability().id() );
            ref.disable();
            ref.capability().remove();
        }
    }

    // @TestAccessible //
    static boolean sameProperties( final CCapability capability1, final CCapability capability2 )
    {
        final List<CCapabilityProperty> p1 = capability1.getProperties();
        final List<CCapabilityProperty> p2 = capability2.getProperties();
        if ( p1 == null )
        {
            return p2 == null;
        }
        else if ( p2 == null )
        {
            return false;
        }
        if ( p1.size() != p2.size() )
        {
            return false;
        }
        return p1.equals( p2 );
    }

}
