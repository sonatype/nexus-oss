package org.sonatype.nexus.plugins;

import org.sonatype.plugin.ExtensionPoint;

/**
 * Extension point for Nexus "plugin entry point". A Nexus plugin bundle does not have to contain this, but if it needs
 * some special lifecycle tasks, then it should.
 * 
 * @author cstamas
 */
@ExtensionPoint
public interface NexusPlugin
{
    /**
     * Called once during plugin lifecycle, on plugin installation. The plugin itself may perform some actions to finish
     * install (actions that are plugin specific).
     * 
     * @param context
     */
    void install( PluginContext context );

    /**
     * Called when Nexus environment activates the plugin. Called multiple times.
     * 
     * @param context
     */
    void init( PluginContext context );

    /**
     * Called only once during plugin lifecycle, on plugin deinstallation. Plugin should undo/clean-up changes made in
     * install() method.
     * 
     * @param context
     */
    void uninstall( PluginContext context );
}
