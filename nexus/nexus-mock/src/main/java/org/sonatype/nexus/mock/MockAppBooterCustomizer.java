package org.sonatype.nexus.mock;

import org.codehaus.plexus.ContainerConfiguration;
import org.sonatype.appbooter.AbstractPlexusAppBooterCustomizer;
import org.sonatype.appbooter.PlexusAppBooter;

public class MockAppBooterCustomizer
    extends AbstractPlexusAppBooterCustomizer
{
    @Override
    public void customizeContainerConfiguration( final PlexusAppBooter appBooter, ContainerConfiguration config )
    {
        config.addComponentDiscoveryListener( new InhibitingComponentDiscovererListener() );
    }
}
