package org.sonatype.nexus.integrationtests;

import java.util.Map.Entry;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.sonatype.appbooter.PlexusAppBooterCustomizer;
import org.sonatype.nexus.test.utils.TestProperties;

public class ITAppBooterCustomizer
    implements PlexusAppBooterCustomizer
{

    public void customizeContainer( PlexusContainer plexusContainer )
    {
    }

    public void customizeContainerConfiguration( ContainerConfiguration cc )
    {
        cc.setContainerConfigurationURL( Class.class.getResource( "/plexus/plexus.xml" ) );
    }

    public void customizeContext( Context ctx )
    {
        for ( Entry<String, String> entry : TestProperties.getAll().entrySet() )
        {
            ctx.put( entry.getKey(), entry.getValue() );
        }
    }

}
