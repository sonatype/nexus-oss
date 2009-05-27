package org.sonatype.nexus.plugins;

import java.util.Map;

/**
 * A high level interface for plugin manager.
 * 
 * @author cstamas
 */
public interface NexusPluginManager
{
    /**
     * Returns the unmodifiable Map of installed plugins.
     * 
     * @return
     */
    Map<String, PluginDescriptor> getInstalledPlugins();

    /**
     * Should be called even before boot process, simply to make plugin-contributed components available to Plexus,
     * since reading up Nexus config may already need those!
     */
    void activateInstalledPlugins();

    /**
     * Called for a given plugin, usually invoked by some user interaction (UI).
     * 
     * @param coords
     */
    void installPlugin( PluginCoordinates coords );

    /**
     * Called for a given plugin, usually invoked by some user interaction (UI).
     * 
     * @param coords
     */
    void uninstallPlugin( PluginCoordinates coords );
}
