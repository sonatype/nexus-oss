package org.sonatype.nexus.plugins;

import java.util.Collection;

import junit.framework.Assert;

import org.codehaus.plexus.component.repository.ComponentDescriptor;

public class Spice21Test
    extends AbstractNexusPluginManagerTest
{
    public void testSpice21InnerClasses()
        throws Exception
    {
        DefaultNexusPluginManager nexusPluginManager =
            (DefaultNexusPluginManager) getContainer().lookup( NexusPluginManager.class );

        // do discovery
        Collection<PluginManagerResponse> activationResponses = nexusPluginManager.activateInstalledPlugins();

        Assert.assertEquals( "Wroung number of plugins discovered!", Assertions.INSTALLED_PLUGINS, activationResponses
            .size() );

        // now we have the nexus-test-plugin with a doubled component (see the plugin)
        ComponentDescriptor<?> outer =
            getContainer().getComponentDescriptor( "org.sonatype.nexus.plugins.spice21.SomeComponent", "A" );

        ComponentDescriptor<?> inner =
            getContainer().getComponentDescriptor( "org.sonatype.nexus.plugins.spice21.SomeComponent", "B" );

        // the plexus handled one should remain
        assertTrue( "Outer class should be gleaned!", outer != null );
        assertEquals( "org.sonatype.nexus.plugins.spice21.ASomeComponent", outer.getImplementation() );

        // runtime gleaning should not happen
        assertTrue( "Inner class should be gleaned!", inner != null );
        assertEquals( "org.sonatype.nexus.plugins.spice21.ASomeComponent$BSomeComponent", inner.getImplementation() );

        assertNotNull( "We should be able to lookup the inner class component!", getContainer().lookup(
            "org.sonatype.nexus.plugins.spice21.SomeComponent", "B" ) );
    }

}
