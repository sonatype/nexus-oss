package org.sonatype.nexus.plugins;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.nexus.plugins.model.PluginMetadata;

public class PluginDiscoveryContext
{
    private final PluginCoordinates pluginCoordinates;

    private final ClassRealm pluginRealm;

    private final ClassRealm dependencyRealm;

    private final PluginMetadata pluginMetadata;

    private final NexusPluginValidator nexusPluginValidator;

    private boolean pluginRegistered = false;

    private PluginDescriptor pluginDescriptor;

    public PluginDiscoveryContext( PluginCoordinates pluginCoordinates, ClassRealm pluginRealm,
                                   ClassRealm dependencyRealm, PluginMetadata pluginMetadata,
                                   NexusPluginValidator nexusPluginValidator )
    {
        this.pluginCoordinates = pluginCoordinates;

        this.pluginRealm = pluginRealm;

        this.dependencyRealm = dependencyRealm;

        this.pluginMetadata = pluginMetadata;

        this.nexusPluginValidator = nexusPluginValidator;
    }

    public PluginCoordinates getPluginCoordinates()
    {
        return pluginCoordinates;
    }

    public ClassRealm getPluginRealm()
    {
        return pluginRealm;
    }

    public ClassRealm getDependencyRealm()
    {
        return dependencyRealm;
    }

    public PluginMetadata getPluginMetadata()
    {
        return pluginMetadata;
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

    public void setPluginDescriptor( PluginDescriptor pluginDescriptor )
    {
        this.pluginDescriptor = pluginDescriptor;
    }
}
