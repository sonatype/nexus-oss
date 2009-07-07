package org.sonatype.nexus.plugins.repository;

import java.io.File;
import java.util.Collection;

import org.sonatype.nexus.plugins.PluginCoordinates;

// TODO: swap File with URL?
public interface PluginRepository
{
    /**
     * Returns a list of available plugin coordinates.
     * 
     * @return
     */
    Collection<PluginCoordinates> findAvailablePlugins();

    /**
     * Returns the file corresponding to supplied coordinates, or null otherwise.
     * 
     * @param coordinates
     * @return the plugin file or null
     */
    File resolvePlugin( PluginCoordinates coordinates );

    /**
     * Result all the dependencies.
     * 
     * @param coordinates
     * @return
     */
    Collection<File> resolvePluginDependencies( PluginCoordinates coordinates );
}
