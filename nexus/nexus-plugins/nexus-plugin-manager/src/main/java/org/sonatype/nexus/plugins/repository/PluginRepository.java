package org.sonatype.nexus.plugins.repository;

import java.util.Collection;

import org.sonatype.plugin.metadata.GAVCoordinate;

/**
 * This is a very high level abstraction for Nexus Plugin Repository. The implementation behind may be something like
 * Local Maven Repository, or even Nexus Repository.
 * 
 * @author cstamas
 */
public interface PluginRepository
{
    /**
     * Returns a list of available plugin coordinates.
     * 
     * @return
     */
    Collection<PluginRepositoryArtifact> findAvailablePlugins();

    /**
     * Returns the artifaft for the given coordinates.
     * 
     * @param coordinates
     * @return
     */
    PluginRepositoryArtifact resolveArtifact( GAVCoordinate coordinates );
}
