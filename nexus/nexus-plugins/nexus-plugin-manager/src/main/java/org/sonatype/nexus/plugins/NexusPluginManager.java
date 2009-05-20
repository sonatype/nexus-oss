package org.sonatype.nexus.plugins;

import java.util.Map;

/**
 * A high level interface for plugin manager.
 * 
 * @author cstamas
 */
public interface NexusPluginManager
{
    Map<String, PluginDescriptor> getInstalledPlugins();
    
    void activatePlugin(String pluginKey);
    
    void activateInstalledPlugins();

    void installPlugin( PluginCoordinates coords );

    void uninstallPlugin( PluginCoordinates coords );
}
