package org.sonatype.nexus.plugins.repository;

import java.util.Collection;

import org.sonatype.plugin.metadata.GAVCoordinate;

public interface PluginRepositoryManager
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
}
