package org.sonatype.nexus.plugins;

public class NoSuchPluginException
    extends Exception
{
    private static final long serialVersionUID = 3012836542082458008L;

    private final PluginCoordinates pluginCoordinates;

    public NoSuchPluginException( PluginCoordinates pluginCoordinates )
    {
        this( pluginCoordinates, "Plugin \"" + pluginCoordinates.toString() + "\" not found." );
    }

    public NoSuchPluginException( PluginCoordinates pluginCoordinates, String message )
    {
        super( message );

        this.pluginCoordinates = pluginCoordinates;
    }

    public PluginCoordinates getPluginCoordinates()
    {
        return pluginCoordinates;
    }
}
