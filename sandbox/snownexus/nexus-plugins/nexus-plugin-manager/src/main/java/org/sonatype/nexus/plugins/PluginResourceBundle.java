package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;
import org.sonatype.plugin.metadata.GAVCoordinate;

/**
 * A PluginResourceBundle component. Even if this class is plexus annotated, the instances of this class are actually
 * injected/crafted (the plexus descriptor for it) manually by NexusPluginDiscoverer.
 * 
 * @author cstamas
 */
public class PluginResourceBundle
    implements NexusResourceBundle
{
    @Requirement
    private NexusPluginManager nexusPluginManager;

    @Configuration( "pluginKey" )
    private String pluginKey;

    protected List<PluginStaticResourceModel> getStaticResourceModels( PluginDescriptor pd )
    {
        ArrayList<PluginStaticResourceModel> models = new ArrayList<PluginStaticResourceModel>();

        if ( !pd.getPluginStaticResourceModels().isEmpty() )
        {
            models.addAll( pd.getPluginStaticResourceModels() );
        }

        return models;
    }

    public List<StaticResource> getContributedResouces()
    {
        GAVCoordinate coord = new GAVCoordinate( pluginKey );

        PluginDescriptor pd = nexusPluginManager.getActivatedPlugins().get( coord );

        if ( pd == null )
        {
            return Collections.emptyList();
        }

        List<PluginStaticResourceModel> models = getStaticResourceModels( pd );

        if ( models.isEmpty() )
        {
            return Collections.emptyList();
        }
        else
        {
            ArrayList<StaticResource> result = new ArrayList<StaticResource>( models.size() );

            for ( PluginStaticResourceModel model : models )
            {
                PluginStaticResource res = new PluginStaticResource( pd.getPluginRealm(), model );

                result.add( res );
            }

            return result;
        }
    }
}
