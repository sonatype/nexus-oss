package org.sonatype.nexus.plugins;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.sonatype.nexus.plugins.model.PluginMetadata;

public class PluginDescriptor
    extends ComponentSetDescriptor
{
    private PluginCoordinates pluginCoordinates;

    private PluginMetadata pluginMetadata;

    private ClassRealm pluginRealm;

    private ClassRealm dependencyRealm;

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

    public ClassRealm getDependencyRealm()
    {
        return dependencyRealm;
    }

    public void setDependencyRealm( ClassRealm dependencyRealm )
    {
        this.dependencyRealm = dependencyRealm;
    }

    public PluginMetadata getPluginMetadata()
    {
        return pluginMetadata;
    }

    public void setPluginMetadata( PluginMetadata pluginMetadata )
    {
        this.pluginMetadata = pluginMetadata;
    }
}
