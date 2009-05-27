package org.sonatype.nexus;

import org.codehaus.plexus.ContainerConfiguration;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.nexus.plugins.plexus.NexusPluginCollector;
import org.sonatype.nexus.plugins.plexus.NexusPluginDiscoverer;

/**
 * The Nexus specific AppBooter.
 * 
 * @author cstamas
 */
public class NexusAppBooter
    extends PlexusAppBooter
{
    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
        containerConfiguration.addComponentDiscoverer( new NexusPluginDiscoverer() );

        containerConfiguration.addComponentDiscoveryListener( new NexusPluginCollector() );
    }
}
