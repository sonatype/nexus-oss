package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.sonatype.plugins.model.PluginMetadata;

public class PluginDescriptor
    extends ComponentSetDescriptor
{
    private PluginCoordinates pluginCoordinates;

    private PluginMetadata pluginMetadata;

    private ClassRealm pluginRealm;

    private List<String> exports;

    private List<PluginDescriptor> importedPlugins;

    private List<PluginStaticResourceModel> pluginStaticResourceModels;

    private Map<String, PluginRepositoryType> pluginRepositoryTypes;

    public PluginCoordinates getPluginCoordinates()
    {
        return pluginCoordinates;
    }

    public void setPluginCoordinates( PluginCoordinates pluginCoordinates )
    {
        this.pluginCoordinates = pluginCoordinates;
    }

    public ClassRealm getPluginRealm()
    {
        return pluginRealm;
    }

    public void setPluginRealm( ClassRealm pluginRealm )
    {
        this.pluginRealm = pluginRealm;
    }

    public PluginMetadata getPluginMetadata()
    {
        return pluginMetadata;
    }

    public void setPluginMetadata( PluginMetadata pluginMetadata )
    {
        this.pluginMetadata = pluginMetadata;
    }

    public List<String> getExports()
    {
        if ( exports == null )
        {
            exports = new ArrayList<String>();
        }

        return exports;
    }

    public List<PluginDescriptor> getImportedPlugins()
    {
        if ( importedPlugins == null )
        {
            importedPlugins = new ArrayList<PluginDescriptor>();
        }

        return importedPlugins;
    }

    public List<PluginStaticResourceModel> getPluginStaticResourceModels()
    {
        if ( pluginStaticResourceModels == null )
        {
            pluginStaticResourceModels = new ArrayList<PluginStaticResourceModel>();
        }

        return pluginStaticResourceModels;
    }

    public Map<String, PluginRepositoryType> getPluginRepositoryTypes()
    {
        if ( pluginRepositoryTypes == null )
        {
            pluginRepositoryTypes = new HashMap<String, PluginRepositoryType>();
        }

        return pluginRepositoryTypes;
    }
}
