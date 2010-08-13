package org.sonatype.nexus.plugins.capabilities.internal.config.events;

import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.plexus.appevents.AbstractEvent;

public class CapabilityConfigurationEvent
    extends AbstractEvent<CCapability>
{
    public CapabilityConfigurationEvent( final CCapability capability )
    {
        super( capability );
    }

    public CCapability getCapability()
    {
        return getEventSender();
    }

}