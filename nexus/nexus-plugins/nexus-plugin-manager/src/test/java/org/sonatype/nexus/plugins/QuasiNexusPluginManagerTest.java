package org.sonatype.nexus.plugins;

import org.codehaus.plexus.ContainerConfiguration;

public class QuasiNexusPluginManagerTest
    extends AbstractNexusPluginManagerTest
{
    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration containerConfiguration )
    {
    }

    public void testQuasiNexus()
        throws Exception
    {
        // all happens in initialize() method, look at QuasiNexus
        lookup( QuasiNexus.class );
    }
}
