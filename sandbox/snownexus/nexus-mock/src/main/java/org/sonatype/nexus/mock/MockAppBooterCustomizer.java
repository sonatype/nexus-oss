package org.sonatype.nexus.mock;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.sonatype.appbooter.PlexusAppBooterCustomizer;

public class MockAppBooterCustomizer
    implements PlexusAppBooterCustomizer
{
    public void customizeContext( Context ctx )
    {
        // no need for this
    }

    public void customizeContainerConfiguration( ContainerConfiguration config )
    {
        config.addComponentDiscoveryListener( new InhibitingComponentDiscovererListener() );
    }

    public void customizeContainer( PlexusContainer container )
    {
        // no need for this
    }
}
