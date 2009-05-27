package org.sonatype.nexus.plugins.plexus;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryEvent;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.sonatype.nexus.plugins.PluginDescriptor;

@Component( role = NexusPluginCollector.class )
public class NexusPluginCollector
    implements ComponentDiscoveryListener
{
    public static final String ID = "nexus-plugin-collector";

    private Map<String, PluginDescriptor> pluginDescriptors = new HashMap<String, PluginDescriptor>();

    public String getId()
    {
        return ID;
    }

    public void componentDiscovered( ComponentDiscoveryEvent event )
    {
        ComponentSetDescriptor componentSetDescriptor = event.getComponentSetDescriptor();

        if ( componentSetDescriptor instanceof PluginDescriptor )
        {
            PluginDescriptor pluginDescriptor = (PluginDescriptor) componentSetDescriptor;

            pluginDescriptors.put( pluginDescriptor.getPluginKey(), pluginDescriptor );
        }
    }

    public PluginDescriptor getPluginDescriptor( String key )
    {
        return pluginDescriptors.get( key );
    }

    public Map<String, PluginDescriptor> getPluginDescriptors()
    {
        return pluginDescriptors;
    }
}
