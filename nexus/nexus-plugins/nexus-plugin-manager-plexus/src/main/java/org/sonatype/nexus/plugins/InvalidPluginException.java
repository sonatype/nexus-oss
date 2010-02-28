package org.sonatype.nexus.plugins;

import org.sonatype.plugin.metadata.GAVCoordinate;

public class InvalidPluginException
    extends Exception
{
    private static final long serialVersionUID = 3012836542082458008L;

    private final GAVCoordinate pluginCoordinates;

    public InvalidPluginException( GAVCoordinate pluginCoordinates, String message )
    {
        super( "Invalid plugin found: " + pluginCoordinates.toString() + ", reason: " + message );

        this.pluginCoordinates = pluginCoordinates;
    }

    public InvalidPluginException( GAVCoordinate pluginCoordinates, Throwable cause )
    {
        super( "Invalid plugin found: " + pluginCoordinates.toString() + ", reason: " + cause.getMessage(), cause );

        this.pluginCoordinates = pluginCoordinates;
    }

    public GAVCoordinate getPluginCoordinates()
    {
        return pluginCoordinates;
    }
}
