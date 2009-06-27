package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.plugins.model.PluginDependency;
import org.sonatype.plugins.model.PluginMetadata;

@Component( role = InterPluginDependencyResolver.class )
public class DefaultInterPluginDependencyResolver
    implements InterPluginDependencyResolver
{
    public List<PluginCoordinates> resolveDependencyPlugins( NexusPluginManager nexusPluginManager,
                                                             PluginMetadata pluginMetadata )
        throws NoSuchPluginException
    {
        ArrayList<PluginCoordinates> result =
            new ArrayList<PluginCoordinates>( pluginMetadata.getPluginDependencies().size() );

        for ( PluginDependency dependency : (List<PluginDependency>) pluginMetadata.getPluginDependencies() )
        {
            PluginCoordinates depCoord =
                new PluginCoordinates( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() );

            // check for existence
            PluginDescriptor dependencyDescriptor =
                nexusPluginManager.getInstalledPlugins().get( depCoord.getPluginKey() );

            if ( dependencyDescriptor != null )
            {
                result.add( depCoord );
            }
            else
            {
                // RECURSION, SOLVE THIS IN MORE ELEGANT WAY
                PluginResponse response = nexusPluginManager.activatePlugin( depCoord );

                if ( !response.isSuccesful() )
                {
                    throw new NoSuchPluginException( depCoord );
                }
                else
                {
                    result.add( depCoord );
                }
            }
        }

        return result;
    }
}
