package org.sonatype.nexus.plugins;

public interface NexusPluginValidator
{
    void validate( PluginDescriptor plugin )
        throws InvalidPluginException;
}
