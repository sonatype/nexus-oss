package org.sonatype.nexus.plugins.repository;

import java.util.Collection;

import org.sonatype.plugin.metadata.GAVCoordinate;

public interface NexusPluginRepository
{
    /**
     * Returns a unique ID for this repository.
     * 
     * @return
     */
    String getId();

    /**
     * The repository priority. Follows the ordering of "natural numbers", so, smaller first then bigger.
     * 
     * @return
     */
    int getPriority();

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
    PluginRepositoryArtifact resolveArtifact( GAVCoordinate coordinates )
        throws NoSuchPluginRepositoryArtifactException;

    /**
     * Returns the artifaft for the given coordinates.
     * 
     * @param coordinates
     * @return
     */
    PluginRepositoryArtifact resolveDependencyArtifact( PluginRepositoryArtifact dependant, GAVCoordinate coordinates )
        throws NoSuchPluginRepositoryArtifactException;
}
