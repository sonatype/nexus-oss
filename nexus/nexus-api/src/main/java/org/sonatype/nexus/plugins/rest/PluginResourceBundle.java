package org.sonatype.nexus.plugins.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.plugins.rest.PluginStaticResource.PluginStaticResourceModel;

public class PluginResourceBundle
    implements NexusResourceBundle
{
    private final ClassLoader classLoader;

    private final String pluginVersion;

    private final List<PluginStaticResourceModel> staticResourceModels = new ArrayList<PluginStaticResourceModel>();

    public PluginResourceBundle( ClassLoader classLoader, String version )
    {
        this.classLoader = classLoader;

        this.pluginVersion = version;
    }

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public String getPluginVersion()
    {
        return pluginVersion;
    }

    public List<PluginStaticResourceModel> getStaticResourceModels()
    {
        return staticResourceModels;
    }

    public List<StaticResource> getContributedResouces()
    {
        ArrayList<StaticResource> result = new ArrayList<StaticResource>( staticResourceModels.size() );

        for ( PluginStaticResourceModel model : staticResourceModels )
        {
            PluginStaticResource res = new PluginStaticResource( getClassLoader(), model );

            result.add( res );
        }

        return result;
    }

    public String getPostBodyContribution( Map<String, Object> context )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPostHeadContribution( Map<String, Object> context )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPreBodyContribution( Map<String, Object> context )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPreHeadContribution( Map<String, Object> context )
    {
        // TODO Auto-generated method stub
        return null;
    }

}
