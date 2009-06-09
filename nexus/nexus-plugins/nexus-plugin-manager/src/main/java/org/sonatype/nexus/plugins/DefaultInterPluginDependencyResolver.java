package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.model.PluginDependency;
import org.sonatype.nexus.plugins.model.PluginMetadata;

@Component( role = InterPluginDependencyResolver.class )
public class DefaultInterPluginDependencyResolver
    implements InterPluginDependencyResolver
{
    public List<PluginCoordinates> resolveDependencyRealms( NexusPluginManager nexusPluginManager,
                                                            PluginMetadata pluginMetadata  )
        throws NoSuchPluginException
    {
        ArrayList<PluginCoordinates> result =
            new ArrayList<PluginCoordinates>( pluginMetadata.getDependencies().size() );

        for ( PluginDependency dependency : (List<PluginDependency>) pluginMetadata.getDependencies() )
        {
            PluginCoordinates depCoord = new PluginCoordinates();

            depCoord.setGroupId( dependency.getGroupId() );

            depCoord.setArtifactId( dependency.getArtifactId() );

            depCoord.setVersion( dependency.getVersion() );

            // check for existence
            PluginDescriptor dependencyDescriptor =
                nexusPluginManager.getInstalledPlugins().get( depCoord.getPluginKey() );

            if ( dependencyDescriptor != null )
            {
                result.add( depCoord );
            }
            else
            {
                throw new NoSuchPluginException( depCoord );
            }
        }

        return result;
    }
}
