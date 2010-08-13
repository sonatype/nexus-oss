package org.sonatype.nexus.plugins.capabilities.internal.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.api.Capability;
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
        final Capability capability = registry.create( capabilityConfig.getId(), capabilityConfig.getTypeId() );
        registry.add( capability );
        capability.create( asMap( capabilityConfig.getProperties() ) );
    }

    private void handle( final CapabilityConfigurationLoadEvent evt )
    {
        final CCapability capabilityConfig = evt.getCapability();
        final Capability capability = registry.create( capabilityConfig.getId(), capabilityConfig.getTypeId() );
        registry.add( capability );
        capability.load( asMap( capabilityConfig.getProperties() ) );
    }

    private void handle( final CapabilityConfigurationUpdateEvent evt )
    {
        final CCapability capabilityConfig = evt.getCapability();
        final Capability capability = registry.get( capabilityConfig.getId() );
        if ( capability != null )
        {
            capability.update( asMap( capabilityConfig.getProperties() ) );
        }
    }

    private void handle( final CapabilityConfigurationRemoveEvent evt )
    {
        final CCapability capabilityConfig = evt.getCapability();
        final Capability capability = registry.get( capabilityConfig.getId() );
        if ( capability != null )
        {
            registry.remove( capability.id() );
            capability.remove();
        }
    }

    private Map<String, String> asMap( final List<CCapabilityProperty> properties )
    {
        final Map<String, String> map = new HashMap<String, String>();
        if ( properties != null )
        {
            for ( final CCapabilityProperty property : properties )
            {
                map.put( property.getKey(), property.getValue() );
            }
        }
        return map;
    }

}
