package org.sonatype.nexus.plugins;

import java.util.Collection;

import junit.framework.Assert;

import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.sonatype.nexus.proxy.events.EventInspector;

public class DoubledComponentsTest
    extends AbstractNexusPluginManagerTest
{

    public void testDoubledArchetypeComponents()
        throws Exception
    {
        DefaultNexusPluginManager nexusPluginManager =
            (DefaultNexusPluginManager) getContainer().lookup( NexusPluginManager.class );

        // do discovery
        Collection<PluginManagerResponse> activationResponses = nexusPluginManager.activateInstalledPlugins();

        Assert.assertEquals( "Wroung number of plugins discovered!", Assertions.INSTALLED_PLUGINS, activationResponses
            .size() );

        // now we have the nexus-test-plugin with a doubled component (see the plugin)
        ComponentDescriptor<?> doubled1 =
            getContainer().getComponentDescriptor( EventInspector.class.getName(), "doubled" );

        ComponentDescriptor<?> doubled2 =
            getContainer().getComponentDescriptor( EventInspector.class.getName(),
                "org.sonatype.nexus.plugins.doubled.DoubledEventInspector" );

        // the plexus handled one should remain
        assertTrue( "Plexus wins if found on a component in plugin!", doubled1 != null );

        // runtime gleaning should not happen
        assertTrue( "Runtime gleaning should not happen!", doubled2 == null );
    }
}
