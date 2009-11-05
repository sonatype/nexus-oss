package org.sonatype.nexus.plugins.plugin.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.plugins.NexusPluginManager;
import org.sonatype.nexus.plugins.PluginResponse;
import org.sonatype.nexus.plugins.plugin.console.model.PluginInfo;
import org.sonatype.nexus.plugins.plugin.console.model.RestInfo;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plugin.metadata.GAVCoordinate;

/**
 * @author juven
 */
@Component( role = PluginConsoleManager.class )
public class DefaultPluginConsoleManager
    extends AbstractLogEnabled
    implements PluginConsoleManager
{
    @Requirement
    private NexusPluginManager pluginManager;

    @Requirement
    private PlexusContainer plexusContainer;

    public List<PluginInfo> listPluginInfo()
    {
        List<PluginInfo> result = new ArrayList<PluginInfo>();

        Map<GAVCoordinate, PluginResponse> pluginResponses = pluginManager.getPluginResponses();

        for ( PluginResponse pluginResponse : pluginResponses.values() )
        {
            result.add( buildPluginInfo( pluginResponse ) );
        }

        return result;
    }

    private PluginInfo buildPluginInfo( PluginResponse pluginResponse )
    {
        PluginInfo result = new PluginInfo();

        result.setStatus( pluginResponse.getAchievedGoal().name() );
        result.setVersion( pluginResponse.getPluginCoordinates().getVersion() );
        if ( pluginResponse.getPluginDescriptor() != null )
        {
            result.setName( pluginResponse.getPluginDescriptor().getPluginMetadata().getName() );
            result.setDescription( pluginResponse.getPluginDescriptor().getPluginMetadata().getDescription() );
            result.setScmVersion( pluginResponse.getPluginDescriptor().getPluginMetadata().getScmVersion() );
            result.setScmTimestamp( pluginResponse.getPluginDescriptor().getPluginMetadata().getScmTimestamp() );
            result.setSite( pluginResponse.getPluginDescriptor().getPluginMetadata().getPluginSite() );
        }
        else
        {
            result.setName( pluginResponse.getPluginCoordinates().getGroupId() + ":"
                + pluginResponse.getPluginCoordinates().getArtifactId() );
        }

        if ( !pluginResponse.isSuccessful() )
        {
            result.setFailureReason( pluginResponse.formatAsString( false ) );
        }

        // WARN
        // dirty hack here, the logic here should be moved into PluginManger
        if ( pluginResponse.isSuccessful() )
        {
            List<String> exportedClassnames = pluginResponse.getPluginDescriptor().getExportedClassnames();

            for ( ComponentDescriptor<?> componentDescriptor : this.plexusContainer
                .getComponentDescriptorList( PlexusResource.class.getName() ) )
            {
                if ( exportedClassnames.contains( componentDescriptor.getImplementation() ) )
                {
                    try
                    {
                        PlexusResource resource = plexusContainer.lookup( PlexusResource.class, componentDescriptor
                            .getRoleHint() );

                        RestInfo restInfo = new RestInfo();
                        restInfo.setUri( resource.getResourceUri() );

                        result.addRestInfo( restInfo );
                    }
                    catch ( ComponentLookupException e )
                    {
                        getLogger().warn(
                            "Unable to find PlexusResource '" + componentDescriptor.getImplementation() + "'.",
                            e );
                    }
                }
            }
        }

        return result;
    }
}
