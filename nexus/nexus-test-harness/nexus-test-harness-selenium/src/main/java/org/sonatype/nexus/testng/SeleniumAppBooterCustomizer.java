package org.sonatype.nexus.testng;

import java.util.Map.Entry;

import org.codehaus.plexus.ContainerConfiguration;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.appcontext.AppContext;
import org.sonatype.nexus.mock.MockAppBooterCustomizer;
import org.sonatype.nexus.test.utils.TestProperties;

public class SeleniumAppBooterCustomizer
    extends MockAppBooterCustomizer
{

    @Override
    public void customizeContext( final PlexusAppBooter appBooter, final AppContext ctx )
    {
        for ( Entry<String, String> entry : TestProperties.getAll().entrySet() )
        {
            ctx.put( entry.getKey(), entry.getValue() );
        }
        super.customizeContext( appBooter, ctx );
    }

    @Override
    public void customizeContainerConfiguration( final PlexusAppBooter appBooter, final ContainerConfiguration config )
    {
        config.setContainerConfigurationURL( Class.class.getResource( "/plexus/plexus.xml" ) );

        super.customizeContainerConfiguration( appBooter, config );
    }
}
