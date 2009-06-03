package org.sonatype.nexus.plugins.repository;

import java.io.File;
import java.util.Collection;

import org.sonatype.nexus.plugins.PluginCoordinates;

/**
 * A very simple abstraction for a local repository-like storage. This is NOT a maven2 local repository!
 * 
 * @author cstamas
 */
public interface NexusPluginRepository
{
    Collection<PluginCoordinates> findAvailablePlugins();

    File resolvePlugin( PluginCoordinates coordinates );

    Collection<File> resolvePluginDependencies( PluginCoordinates coordinates );
}
