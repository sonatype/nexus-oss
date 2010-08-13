package org.sonatype.nexus.plugins.capabilities.internal.config.events;

import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;

public class CapabilityConfigurationUpdateEvent
    extends CapabilityConfigurationEvent
{

    public CapabilityConfigurationUpdateEvent( final CCapability capability )
    {
        super( capability );
    }

}
