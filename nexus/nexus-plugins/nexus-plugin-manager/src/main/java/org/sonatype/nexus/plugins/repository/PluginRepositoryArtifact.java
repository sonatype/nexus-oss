package org.sonatype.nexus.plugins.repository;

import java.io.File;

import org.sonatype.plugin.metadata.GAVCoordinate;

public class PluginRepositoryArtifact
{
    private NexusPluginRepository nexusPluginRepository;

    private GAVCoordinate coordinate;

    private File file;
    
    public NexusPluginRepository getNexusPluginRepository()
    {
        return nexusPluginRepository;
    }

    public void setNexusPluginRepository( NexusPluginRepository nexusPluginRepository )
    {
        this.nexusPluginRepository = nexusPluginRepository;
    }

    public GAVCoordinate getCoordinate()
    {
        return coordinate;
    }

    public void setCoordinate( GAVCoordinate pluginCoordinate )
    {
        this.coordinate = pluginCoordinate;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile( File file )
    {
        this.file = file;
    }
}
