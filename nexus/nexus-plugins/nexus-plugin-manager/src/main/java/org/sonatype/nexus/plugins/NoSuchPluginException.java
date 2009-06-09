package org.sonatype.nexus.plugins;

public class NoSuchPluginException
    extends Exception
{
    private static final long serialVersionUID = 3012836542082458008L;

    private final PluginCoordinates pluginCoordinates;

    public NoSuchPluginException( PluginCoordinates pluginCoordinates )
    {
        super( "No plugin found: " + pluginCoordinates.toString() );

        this.pluginCoordinates = pluginCoordinates;
    }

    public PluginCoordinates getPluginCoordinates()
    {
        return pluginCoordinates;
    }
}
