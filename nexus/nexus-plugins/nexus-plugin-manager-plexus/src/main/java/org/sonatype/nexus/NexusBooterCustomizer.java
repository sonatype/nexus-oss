package org.sonatype.nexus;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.sonatype.appbooter.AbstractPlexusAppBooterCustomizer;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.appcontext.AppContext;
import org.sonatype.nexus.plugins.plexus.NexusPluginsComponentRepository;

public class NexusBooterCustomizer
    extends AbstractPlexusAppBooterCustomizer
{
    public void customizeContext( final PlexusAppBooter booter, final AppContext context )
    {
        context.put( ComponentRepository.class.getName(), new NexusPluginsComponentRepository() );
    }

    public void customizeContainerConfiguration( final PlexusAppBooter booter,
                                                 final ContainerConfiguration containerConfiguration )
    {
        containerConfiguration.setComponentRepository( (ComponentRepository) containerConfiguration.getContext().get( ComponentRepository.class.getName() ) );
    }

}
