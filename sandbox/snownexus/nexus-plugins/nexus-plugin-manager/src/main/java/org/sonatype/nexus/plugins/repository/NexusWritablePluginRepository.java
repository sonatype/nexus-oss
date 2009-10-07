package org.sonatype.nexus.plugins.repository;

import java.io.File;
import java.io.IOException;

import org.sonatype.plugin.metadata.GAVCoordinate;

public interface NexusWritablePluginRepository
    extends NexusPluginRepository
{
    /**
     * Installs a plugin bundle into plugin repository.
     * 
     * @param bundle
     * @return
     * @throws IOException
     */
    boolean installPluginBundle( File bundle )
        throws IOException;

    /**
     * Deletes the plugin from plugin repository.
     * 
     * @param coordinates
     * @return
     * @throws IOException
     */
    boolean deletePluginBundle( GAVCoordinate coordinates )
        throws IOException;
}
