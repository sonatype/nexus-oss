package org.sonatype.nexus.plugins;

import org.sonatype.plugin.metadata.GAVCoordinate;

public class NoSuchPluginException
    extends Exception
{
    private static final long serialVersionUID = 3012836542082458008L;

    private final GAVCoordinate pluginCoordinates;

    public NoSuchPluginException( GAVCoordinate pluginCoordinates )
    {
        this( pluginCoordinates, "Plugin \"" + pluginCoordinates.toString() + "\" not found." );
    }

    public NoSuchPluginException( GAVCoordinate pluginCoordinates, String message )
    {
        super( message );

        this.pluginCoordinates = pluginCoordinates;
    }

    public GAVCoordinate getPluginCoordinates()
    {
        return pluginCoordinates;
    }
}
