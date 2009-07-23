package org.sonatype.nexus.plugins.repository;

import java.io.File;
import java.io.IOException;

import org.sonatype.plugin.metadata.GAVCoordinate;

public abstract class AbstractWritableFileNexusPluginRepository
    extends AbstractFileNexusPluginRepository
    implements NexusWritablePluginRepository
{
    public boolean deletePluginBundle( GAVCoordinate coordinates )
        throws IOException
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean installPluginBundle( File bundle )
        throws IOException
    {
        // TODO Auto-generated method stub
        return false;
    }
}
