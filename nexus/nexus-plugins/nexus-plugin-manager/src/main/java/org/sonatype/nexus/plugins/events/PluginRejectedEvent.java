package org.sonatype.nexus.plugins.events;

import org.sonatype.nexus.plugins.NexusPluginManager;
import org.sonatype.nexus.plugins.NexusPluginValidator;
import org.sonatype.nexus.plugins.PluginDescriptor;
import org.sonatype.plexus.appevents.AbstractEvent;

public class PluginRejectedEvent
    extends AbstractEvent<NexusPluginManager>
{
    private final PluginDescriptor pluginDescriptor;

    private final NexusPluginValidator validator;

    public PluginRejectedEvent( NexusPluginManager component, PluginDescriptor pluginDescriptor,
                                NexusPluginValidator validator )
    {
        super( component );

        this.pluginDescriptor = pluginDescriptor;

        this.validator = validator;
    }

    public String getPluginKey()
    {
        return getPluginDescriptor().getPluginKey();
    }

    public PluginDescriptor getPluginDescriptor()
    {
        return pluginDescriptor;
    }

    public NexusPluginValidator getNexusPluginValidator()
    {
        return validator;
    }

    public NexusPluginManager getNexusPluginManager()
    {
        return getEventSender();
    }
}
