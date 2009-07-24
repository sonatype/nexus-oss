package org.sonatype.nexus.plugins.repository;

import java.util.Map;

import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

public interface PluginRepositoryManager
{
    /**
     * Adds in "manual" fashion a repository.
     * 
     * @param repository
     */
    void addCustomNexusPluginRepository( NexusPluginRepository repository );

    /**
     * Removed in "manual" fashion a repository.
     * 
     * @param id
     */
    void removeCustomNexusPluginRepository( String id );

    /**
     * Gets the repository with ID.
     * 
     * @param id
     * @return
     */
    NexusPluginRepository getNexusPluginRepository( String id );

    /**
     * Returns a list of available plugin coordinates.
     * 
     * @return
     */
    Map<GAVCoordinate, PluginMetadata> findAvailablePlugins();

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
