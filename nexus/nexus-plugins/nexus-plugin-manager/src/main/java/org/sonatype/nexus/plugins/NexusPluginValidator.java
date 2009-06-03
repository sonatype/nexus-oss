package org.sonatype.nexus.plugins;

public interface NexusPluginValidator
{
    boolean validate( PluginDescriptor plugin );
}
