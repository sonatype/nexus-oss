package org.sonatype.nexus.plugins.events;

import org.sonatype.nexus.plugins.NexusPluginManager;
import org.sonatype.nexus.plugins.PluginDescriptor;
import org.sonatype.plexus.appevents.AbstractEvent;

public class PluginRejectedEvent
    extends AbstractEvent<NexusPluginManager>
{
    private final PluginDescriptor pluginDescriptor;

    public PluginRejectedEvent( NexusPluginManager component, PluginDescriptor pluginDescriptor )
    {
        super( component );

        this.pluginDescriptor = pluginDescriptor;
    }

    public String getPluginKey()
    {
        return getPluginDescriptor().getPluginKey();
    }

    public PluginDescriptor getPluginDescriptor()
    {
        return pluginDescriptor;
    }

    public NexusPluginManager getNexusPluginManager()
    {
        return getEventSender();
    }
}
