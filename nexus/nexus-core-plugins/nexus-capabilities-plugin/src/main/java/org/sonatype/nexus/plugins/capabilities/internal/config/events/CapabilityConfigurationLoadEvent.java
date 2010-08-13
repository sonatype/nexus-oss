package org.sonatype.nexus.plugins.capabilities.internal.config.events;

import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;

public class CapabilityConfigurationLoadEvent
    extends CapabilityConfigurationEvent
{

    public CapabilityConfigurationLoadEvent( final CCapability capability )
    {
        super( capability );
    }

}
