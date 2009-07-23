package org.sonatype.nexus.plugins;

import org.sonatype.plugin.metadata.GAVCoordinate;

public class DependencyNotFoundException
    extends NoSuchPluginException
{
    private static final long serialVersionUID = 8009245165491672038L;

    private final GAVCoordinate dependencyCoordinate;

    public DependencyNotFoundException( GAVCoordinate pluginCoordinates, GAVCoordinate dependencyCoordinates )
    {
        this( pluginCoordinates, dependencyCoordinates, "Dependency \"" + dependencyCoordinates.toString()
            + "\" required by \"" + pluginCoordinates.toString() + "\" not found." );
    }

    public DependencyNotFoundException( GAVCoordinate pluginCoordinates, GAVCoordinate dependencyCoordinates,
                                        String message )
    {
        super( pluginCoordinates, message );

        this.dependencyCoordinate = dependencyCoordinates;
    }

    public GAVCoordinate getDependencyCoordinate()
    {
        return dependencyCoordinate;
    }
}
