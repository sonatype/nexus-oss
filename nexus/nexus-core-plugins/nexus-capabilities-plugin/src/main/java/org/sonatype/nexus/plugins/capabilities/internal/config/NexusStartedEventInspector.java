package org.sonatype.nexus.plugins.capabilities.internal.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.plexus.appevents.Event;

@Singleton
public class NexusStartedEventInspector
    implements EventInspector
{

    private final CapabilityConfiguration capabilitiesConfiguration;

    @Inject
    public NexusStartedEventInspector( final CapabilityConfiguration capabilitiesConfiguration )
    {
        this.capabilitiesConfiguration = capabilitiesConfiguration;
    }

    public boolean accepts( final Event<?> evt )
    {
        return evt != null
            && evt instanceof NexusStartedEvent;
    }

    public void inspect( final Event<?> evt )
    {
        if ( !accepts( evt ) )
        {
            return;
        }
        try
        {
            capabilitiesConfiguration.load();
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( "Could not load configurations", e );
        }
    }

}
