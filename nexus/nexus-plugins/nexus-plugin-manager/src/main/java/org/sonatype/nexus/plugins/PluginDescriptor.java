package org.sonatype.nexus.plugins;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.sonatype.nexus.plugins.model.PluginMetadata;

public class PluginDescriptor
    extends ComponentSetDescriptor
{
    private String pluginKey;

    private PluginMetadata pluginMetadata;

    private ClassRealm classRealm;

    public String getPluginKey()
    {
        return pluginKey;
    }

    public void setPluginKey( String pluginKey )
    {
        this.pluginKey = pluginKey;
    }

    public ClassRealm getClassRealm()
    {
        return classRealm;
    }

    public void setClassRealm( ClassRealm classRealm )
    {
        this.classRealm = classRealm;
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
