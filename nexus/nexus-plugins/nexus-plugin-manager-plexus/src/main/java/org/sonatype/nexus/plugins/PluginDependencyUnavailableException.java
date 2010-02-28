package org.sonatype.nexus.plugins;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plugin.metadata.GAVCoordinate;

public class PluginDependencyUnavailableException
    extends Exception
{
    private static final long serialVersionUID = 6888426895432911234L;

    private final List<GAVCoordinate> dependencyPluginsCoordinates;

    public PluginDependencyUnavailableException( List<GAVCoordinate> dependencyPluginsCoordinates )
    {
        this( dependencyPluginsCoordinates, null );
    }

    public PluginDependencyUnavailableException( List<GAVCoordinate> dependencyPluginsCoordinates, String message )
    {
        super( message );

        this.dependencyPluginsCoordinates = dependencyPluginsCoordinates;
    }

    public List<GAVCoordinate> getDependencyCoordinates()
    {
        return dependencyPluginsCoordinates;
    }

    @Override
    public String getMessage()
    {
        if ( StringUtils.isEmpty( super.getMessage() ) )
        {
            StringBuilder msg = new StringBuilder();

            msg.append( "Follwing dependency plugins are unavailable:" );

            for ( GAVCoordinate pluginCoord : dependencyPluginsCoordinates )
            {
                msg.append( " '" + pluginCoord.toString() + "' " );
            }

            return msg.toString();
        }

        return super.getMessage();
    }

}
