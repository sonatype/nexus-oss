package org.sonatype.nexus.plugins.events;

import org.sonatype.nexus.plugins.NexusPluginManager;
import org.sonatype.plexus.appevents.AbstractEvent;
import org.sonatype.plugin.metadata.GAVCoordinate;

public class PluginRejectedEvent
    extends AbstractEvent<NexusPluginManager>
{
    private final GAVCoordinate pluginCoordinates;

    private final Throwable reason;

    public PluginRejectedEvent( NexusPluginManager component, GAVCoordinate pluginCoordinates, Throwable reason )
    {
        super( component );

        this.pluginCoordinates = pluginCoordinates;

        this.reason = reason;
    }

    public GAVCoordinate getPluginCoordinates()
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
