package org.sonatype.nexus.plugins;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

@Component( role = NexusPluginManager.class )
public class DefaultNexusPluginManager
    implements NexusPluginManager
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private NexusPluginCollector nexusPluginCollector;

    public void discoverPlugins( File localRepository )
    {
        // TODO Auto-generated method stub

    }

    public Map<String, PluginDescriptor> getInstalledPlugins()
    {
        return nexusPluginCollector.getPluginDescriptors();
    }

    public void installPlugin( URL source )
    {
        // TODO Auto-generated method stub

    }

}
