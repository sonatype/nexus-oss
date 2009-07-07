package org.sonatype.nexus.plugins.repository;

public interface PluginRepositoryManager
    extends PluginRepository
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
}
