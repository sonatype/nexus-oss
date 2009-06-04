package org.sonatype.nexus;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.sonatype.appbooter.jsw.PlexusAppBooterJSWListener;
import org.sonatype.nexus.plugins.NexusPluginManager;

/**
 * The Nexus specific AppBooter.
 * 
 * @author cstamas
 */
public class NexusAppBooter
    extends PlexusAppBooterJSWListener
{
    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
    }

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
        super.customizeContainerConfiguration( containerConfiguration );
        
        containerConfiguration.addComponentDiscoverer( NexusPluginManager.class )
            .addComponentDiscoveryListener( NexusPluginManager.class );
    }

    protected void customizeContainer( PlexusContainer plexusContainer )
    {
        super.customizeContainer( plexusContainer );
    }
}
