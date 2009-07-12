package org.sonatype.nexus.plugins.repository;

public interface NexusPluginRepository
    extends PluginRepository
{
    String getId();

    int getPriority();
}
