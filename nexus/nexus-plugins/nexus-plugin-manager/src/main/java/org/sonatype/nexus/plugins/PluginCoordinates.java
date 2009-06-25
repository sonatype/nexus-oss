package org.sonatype.nexus.plugins;

import org.sonatype.plugin.metadata.GAVCoordinate;

public class PluginCoordinates
    extends GAVCoordinate
{
    public PluginCoordinates()
    {
        super();
    }

    public PluginCoordinates( String groupId, String artifactId, String version )
    {
        super( groupId, artifactId, version );
    }

    public PluginCoordinates( String composite )
        throws IllegalArgumentException
    {
        super( composite );
    }

    public String getPluginKey()
    {
        // for now, it is enuf
        return toString();
    }
}
