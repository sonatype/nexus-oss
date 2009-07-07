package org.sonatype.nexus.plugins.repository;

/**
 * A very simple abstraction for a local repository-like storage. This is NOT a maven2 local repository!
 * 
 * @author cstamas
 */
public interface NexusPluginRepository
    extends PluginRepository
{
    String getId();
}
