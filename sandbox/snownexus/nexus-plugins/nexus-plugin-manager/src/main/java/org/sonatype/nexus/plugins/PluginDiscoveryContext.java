package org.sonatype.nexus.plugins;

public class PluginDiscoveryContext
{
    private final NexusPluginValidator nexusPluginValidator;

    private final PluginDescriptor pluginDescriptor;

    private boolean pluginRegistered = false;

    public PluginDiscoveryContext( PluginDescriptor pluginDescriptor, NexusPluginValidator nexusPluginValidator )
    {
        this.nexusPluginValidator = nexusPluginValidator;

        this.pluginDescriptor = pluginDescriptor;

        this.pluginRegistered = false;
    }

    public NexusPluginValidator getNexusPluginValidator()
    {
        return nexusPluginValidator;
    }

    public boolean isPluginRegistered()
    {
        return pluginRegistered;
    }

    public void setPluginRegistered( boolean pluginRegistered )
    {
        this.pluginRegistered = pluginRegistered;
    }

    public PluginDescriptor getPluginDescriptor()
    {
        return pluginDescriptor;
    }
}
