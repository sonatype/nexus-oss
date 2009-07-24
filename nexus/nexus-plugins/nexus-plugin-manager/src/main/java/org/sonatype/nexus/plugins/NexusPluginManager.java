package org.sonatype.nexus.plugins;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

/**
 * A high level interface for plugin manager.
 * 
 * @author cstamas
 */
public interface NexusPluginManager
{
    /**
     * Returns the unmodifiable Map of activated plugins.
     * 
     * @return
     */
    Map<GAVCoordinate, PluginDescriptor> getActivatedPlugins();

    /**
     * Returns the unmodifiable Map of installed plugins (includes activated ones too!).
     * 
     * @return
     */
    Map<GAVCoordinate, PluginMetadata> getInstalledPlugins();

    /**
     * Returns the unmodifiable Map of plugin actions that PM executed in during latest runtime.
     * 
     * @return
     */
    Map<GAVCoordinate, PluginResponse> getPluginResponses();

    /**
     * Should be called even before boot process, simply to make plugin-contributed components available to Plexus,
     * since reading up Nexus config may already need those! It tries to activate all locally available (installed)
     * plugins at once.
     */
    Collection<PluginManagerResponse> activateInstalledPlugins();

    /**
     * Queries is plugin activated.
     * 
     * @param coords
     * @return
     */
    boolean isActivatedPlugin( GAVCoordinate coords );

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
