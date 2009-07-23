package org.sonatype.nexus.plugins;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.sonatype.plugin.metadata.GAVCoordinate;

/**
 * A high level interface for plugin manager.
 * 
 * @author cstamas
 */
public interface NexusPluginManager
{
    /**
     * Creates a plugin key from coordinates.
     * 
     * @param coordinate
     * @return
     */
    String getPluginKey( GAVCoordinate coordinate );

    /**
     * Returns the unmodifiable Map of installed plugins.
     * 
     * @return
     */
    Map<String, PluginDescriptor> getActivatedPlugins();

    /**
     * Returns the unmodifiable Map of available plugins.
     * 
     * @return
     */
    Map<String, PluginDescriptor> getAvailablePlugins();

    /**
     * Should be called even before boot process, simply to make plugin-contributed components available to Plexus,
     * since reading up Nexus config may already need those!
     */
    Collection<PluginManagerResponse> activateInstalledPlugins();

    /**
     * Activates the given plugin.
     * 
     * @param coords
     * @return
     */
    PluginManagerResponse activatePlugin( GAVCoordinate coords );

    /**
     * Deactivates the given plugin.
     * 
     * @param coords
     * @return
     */
    PluginManagerResponse deactivatePlugin( GAVCoordinate coords );

    /**
     * Called for a given plugin, usually invoked by some user interaction (UI). Installs the plugin, but does not
     * activate it.
     * 
     * @param coords
     */
    boolean installPluginBundle( File bundle )
        throws IOException;

    /**
     * Called for a given plugin, usually invoked by some user interaction (UI). Deactivates if needed and uninstall the
     * plugin.
     * 
     * @param coords
     */
    boolean uninstallPluginBundle( GAVCoordinate coords )
        throws IOException;
}
