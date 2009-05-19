package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.PluginStaticResource.PluginStaticResourceModel;
import org.sonatype.nexus.plugins.model.Resource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;

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
    private NexusPluginCollector nexusPluginCollector;

    @Configuration( "pluginKey" )
    private String pluginKey;

    @SuppressWarnings( "unchecked" )
    protected List<PluginStaticResourceModel> getStaticResourceModels( PluginDescriptor pd )
    {
        ArrayList<PluginStaticResourceModel> models = new ArrayList<PluginStaticResourceModel>();

        if ( !pd.getPluginMetadata().getResources().isEmpty() )
        {
            for ( Resource res : (List<Resource>) pd.getPluginMetadata().getResources() )
            {
                PluginStaticResourceModel model =
                    new PluginStaticResourceModel( res.getResourcePath(), res.getPublishedPath(), res.getContentType() );

                models.add( model );
            }
        }

        return models;
    }

    public List<StaticResource> getContributedResouces()
    {
        PluginDescriptor pd = nexusPluginCollector.getPluginDescriptor( pluginKey );

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
                PluginStaticResource res = new PluginStaticResource( pd.getClassRealm(), model );

                result.add( res );
            }

            return result;
        }
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
