package org.sonatype.nexus.plugins.repository;

import java.util.Map;

import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

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
    Map<GAVCoordinate, PluginMetadata> findAvailablePlugins();

    /**
     * Returns the artifact for the given coordinates.
     * 
     * @param coordinates
     * @return
     */
    PluginRepositoryArtifact resolveArtifact( GAVCoordinate coordinates )
        throws NoSuchPluginRepositoryArtifactException;

    /**
     * Returns the artifact for the given coordinates.
     * 
     * @param coordinates
     * @return
     */
    PluginRepositoryArtifact resolveDependencyArtifact( PluginRepositoryArtifact dependant, GAVCoordinate coordinates )
        throws NoSuchPluginRepositoryArtifactException;

    /**
     * Returns plugin metadata for the given coordinates.
     */
    PluginMetadata getPluginMetadata( GAVCoordinate coordinates ) throws NoSuchPluginRepositoryArtifactException;
}
