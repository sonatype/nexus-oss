package org.sonatype.nexus.plugins.capabilities.internal.config.events;

import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;

public class CapabilityConfigurationRemoveEvent
    extends CapabilityConfigurationEvent
{

    public CapabilityConfigurationRemoveEvent( final CCapability capability )
    {
        super( capability );
    }

}
