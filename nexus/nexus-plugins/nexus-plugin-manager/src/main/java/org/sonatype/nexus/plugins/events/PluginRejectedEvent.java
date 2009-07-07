package org.sonatype.nexus.plugins.events;

import org.sonatype.nexus.plugins.NexusPluginManager;
import org.sonatype.nexus.plugins.PluginCoordinates;
import org.sonatype.plexus.appevents.AbstractEvent;

public class PluginRejectedEvent
    extends AbstractEvent<NexusPluginManager>
{
    private final PluginCoordinates pluginCoordinates;

    private final Throwable reason;

    public PluginRejectedEvent( NexusPluginManager component, PluginCoordinates pluginCoordinates, Throwable reason )
    {
        super( component );

        this.pluginCoordinates = pluginCoordinates;

        this.reason = reason;
    }

    public PluginCoordinates getPluginCoordinates()
    {
        return pluginCoordinates;
    }

    public Throwable getReason()
    {
        return reason;
    }

    public NexusPluginManager getNexusPluginManager()
    {
        return getEventSender();
    }
}
