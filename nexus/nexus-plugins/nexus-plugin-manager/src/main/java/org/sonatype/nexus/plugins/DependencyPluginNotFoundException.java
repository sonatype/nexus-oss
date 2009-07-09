package org.sonatype.nexus.plugins;

public class DependencyPluginNotFoundException
    extends NoSuchPluginException
{
    private static final long serialVersionUID = 8009245165491672038L;

    private final PluginCoordinates dependantCoordinates;

    public DependencyPluginNotFoundException( PluginCoordinates pluginCoordinates,
                                              PluginCoordinates dependencyCoordinates )
    {
        this( pluginCoordinates, dependencyCoordinates, "Dependency \"" + dependencyCoordinates.toString()
            + "\" required by \"" + pluginCoordinates.toString() + "\" not found." );
    }

    public DependencyPluginNotFoundException( PluginCoordinates pluginCoordinates,
                                              PluginCoordinates dependencyCoordinates, String message )
    {
        super( dependencyCoordinates, message );

        this.dependantCoordinates = pluginCoordinates;
    }

    public PluginCoordinates getDependantCoordinates()
    {
        return dependantCoordinates;
    }
}
