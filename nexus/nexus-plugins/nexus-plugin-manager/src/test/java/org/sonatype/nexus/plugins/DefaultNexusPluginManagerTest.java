package org.sonatype.nexus.plugins;

public class DefaultNexusPluginManagerTest
    extends AbstractNexusPluginManagerTest
{
    public void testSimple()
        throws Exception
    {
        Assertions assertions = new Assertions( getContainer() );

        assertions.doCheck();
    }
}
