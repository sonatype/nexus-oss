package org.sonatype.nexus.plugins;

import java.io.File;
import java.net.URL;
import java.util.Map;

/**
 * A high level interface for plugin manager.
 * 
 * @author cstamas
 */
public interface NexusPluginManager
{
    /**
     * Lists the installed plugins.
     * 
     * @return
     */
    Map<String, PluginDescriptor> getInstalledPlugins();

    void discoverPlugins( File localRepository );

    /**
     * Installs a plugin from remote location???
     * 
     * @param source
     */
    void installPlugin( URL source );

}
