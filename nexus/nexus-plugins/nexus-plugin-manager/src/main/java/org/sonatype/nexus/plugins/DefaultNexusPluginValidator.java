package org.sonatype.nexus.plugins;

public class DefaultNexusPluginValidator
    implements NexusPluginValidator
{
    public boolean validate( PluginDescriptor plugin )
    {
        return true;
    }
}
