package org.sonatype.nexus.plugins.capabilities.internal.config.events;

import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;

public class CapabilityConfigurationAddEvent
    extends CapabilityConfigurationEvent
{

    public CapabilityConfigurationAddEvent( final CCapability capability )
    {
        super( capability );
    }

}
