package org.sonatype.nexus.plugins.repository;

public interface NexusPluginRepository
    extends PluginRepository
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
}
