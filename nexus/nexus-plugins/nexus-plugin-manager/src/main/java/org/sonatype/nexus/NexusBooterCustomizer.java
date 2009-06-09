package org.sonatype.nexus;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.sonatype.appbooter.PlexusAppBooterCustomizer;
import org.sonatype.nexus.plugins.NexusPluginsComponentRepository;

public class NexusBooterCustomizer
    implements PlexusAppBooterCustomizer
{
    public void customizeContext( Context context )
    {
    }

    public void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
        containerConfiguration.setComponentRepository( new NexusPluginsComponentRepository() );
    }

    public void customizeContainer( PlexusContainer plexusContainer )
    {
    }
}
