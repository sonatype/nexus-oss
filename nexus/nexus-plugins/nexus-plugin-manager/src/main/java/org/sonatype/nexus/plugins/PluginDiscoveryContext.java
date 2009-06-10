package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.nexus.plugins.model.PluginMetadata;

public class PluginDiscoveryContext
{
    private final PluginCoordinates pluginCoordinates;

    private final List<String> exports;

    private final ClassRealm pluginRealm;

    private final PluginMetadata pluginMetadata;

    private final NexusPluginValidator nexusPluginValidator;

    private boolean pluginRegistered = false;

    private PluginDescriptor pluginDescriptor;

    private List<PluginDescriptor> importedPlugins;

    public PluginDiscoveryContext( PluginCoordinates pluginCoordinates, List<String> exports, ClassRealm pluginRealm,
                                   PluginMetadata pluginMetadata, NexusPluginValidator nexusPluginValidator )
    {
        this.pluginCoordinates = pluginCoordinates;

        this.exports = exports;

        this.pluginRealm = pluginRealm;

        this.pluginMetadata = pluginMetadata;

        this.nexusPluginValidator = nexusPluginValidator;
    }

    public PluginCoordinates getPluginCoordinates()
    {
        return pluginCoordinates;
    }

    public List<String> getExports()
    {
        return exports;
    }

    public ClassRealm getPluginRealm()
    {
        return pluginRealm;
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

    public List<PluginDescriptor> getImportedPlugins()
    {
        if ( importedPlugins == null )
        {
            importedPlugins = new ArrayList<PluginDescriptor>();
        }

        return importedPlugins;
    }
}
