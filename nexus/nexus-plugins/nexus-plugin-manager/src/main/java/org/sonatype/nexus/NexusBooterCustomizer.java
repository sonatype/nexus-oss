package org.sonatype.nexus;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.context.Context;
import org.sonatype.appbooter.PlexusAppBooterCustomizer;
import org.sonatype.nexus.plugins.NexusPluginsComponentRepository;

public class NexusBooterCustomizer
    implements PlexusAppBooterCustomizer
{
    public void customizeContext( Context context )
    {
        context.put( ComponentRepository.class.getName(), new NexusPluginsComponentRepository() );
    }

    public void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
        containerConfiguration.setComponentRepository( (ComponentRepository) containerConfiguration.getContext()
            .get( ComponentRepository.class.getName() ) );
    }

    public void customizeContainer( PlexusContainer plexusContainer )
    {
    }
}
