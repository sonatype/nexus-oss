package org.sonatype.nexus.plugins;

public class InvalidPluginException
    extends Exception
{
    private static final long serialVersionUID = 3012836542082458008L;

    private final PluginCoordinates pluginCoordinates;

    public InvalidPluginException( PluginCoordinates pluginCoordinates, String message )
    {
        super( "Invalid plugin found: " + pluginCoordinates.toString() + ", reason: " + message );

        this.pluginCoordinates = pluginCoordinates;
    }

    public InvalidPluginException( PluginCoordinates pluginCoordinates, Throwable cause )
    {
        super( "Invalid plugin found: " + pluginCoordinates.toString() + ", reason: " + cause.getMessage(), cause );

        this.pluginCoordinates = pluginCoordinates;
    }

    public PluginCoordinates getPluginCoordinates()
    {
        return pluginCoordinates;
    }
}
