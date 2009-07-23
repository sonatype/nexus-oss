package org.sonatype.nexus.plugins.repository;

import org.sonatype.plugin.metadata.GAVCoordinate;

public class NoSuchPluginRepositoryArtifactException
    extends Exception
{
    private static final long serialVersionUID = 5107708167406593369L;

    private final NexusPluginRepository nexusPluginRepository;

    private final GAVCoordinate coordinate;

    public NoSuchPluginRepositoryArtifactException( NexusPluginRepository nexusPluginRepository,
                                                    GAVCoordinate coordinate )
    {
        super( "Plugin \"" + coordinate.toString() + "\" not found in repository \"" + nexusPluginRepository.getId()
            + "\"!" );

        this.nexusPluginRepository = nexusPluginRepository;

        this.coordinate = coordinate;
    }

    public NexusPluginRepository getNexusPluginRepository()
    {
        return nexusPluginRepository;
    }

    public GAVCoordinate getCoordinate()
    {
        return coordinate;
    }
}
