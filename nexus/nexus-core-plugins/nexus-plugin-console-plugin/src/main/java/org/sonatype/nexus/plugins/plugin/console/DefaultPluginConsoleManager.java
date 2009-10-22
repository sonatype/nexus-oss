package org.sonatype.nexus.plugins.plugin.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.NexusPluginManager;
import org.sonatype.nexus.plugins.PluginResponse;
import org.sonatype.nexus.plugins.plugin.console.model.PluginInfo;
import org.sonatype.plugin.metadata.GAVCoordinate;

/**
 * @author juven
 */
@Component( role = PluginConsoleManager.class )
public class DefaultPluginConsoleManager
    implements PluginConsoleManager
{
    @Requirement
    private NexusPluginManager pluginManager;

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
        if ( pluginResponse.isSuccessful() )
        {
            result.setName( pluginResponse.getPluginDescriptor().getPluginMetadata().getName() );
            result.setDescription( pluginResponse.getPluginDescriptor().getPluginMetadata().getDescription() );

        }
        else
        {
            result.setName( pluginResponse.getPluginCoordinates().toString() );
            result.setFailureReason( pluginResponse.formatAsString( false ) );
        }

        return result;
    }

}
