package org.sonatype.nexus.testng;

import java.util.Map.Entry;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.context.Context;
import org.sonatype.nexus.mock.MockAppBooterCustomizer;
import org.sonatype.nexus.test.utils.TestProperties;

public class SeleniumAppBooterCustomizer
    extends MockAppBooterCustomizer
{

    @Override
    public void customizeContext( Context ctx )
    {
        for ( Entry<String, String> entry : TestProperties.getAll().entrySet() )
        {
            ctx.put( entry.getKey(), entry.getValue() );
        }
        super.customizeContext( ctx );
    }

    @Override
    public void customizeContainerConfiguration( ContainerConfiguration cc )
    {
        cc.setContainerConfigurationURL( Class.class.getResource( "/plexus/plexus.xml" ) );

        super.customizeContainerConfiguration( cc );
    }
}
