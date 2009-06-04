package org.sonatype.nexus.plugins;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;

public class DefaultNexusPluginManagerTest
    extends PlexusTestCase
{
    protected DefaultNexusPluginManager nexusPluginManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusPluginManager = (DefaultNexusPluginManager) lookup( NexusPluginManager.class );
    }

    @Override
    protected void customizeContext( Context context )
    {
        context.put( "nexus-work", new File( getBasedir() + "src/test " ) );
    }

    public void testSimple()
        throws Exception
    {
        MockComponent mc = getContainer().lookup( MockComponent.class );

        // record pre-discovery state
        int customizersPre = mc.getCustomizers().size();
        int processorsPre = mc.getProcessors().size();

        // do discovery, both recorded value should be +1
        PluginManagerResponse response = nexusPluginManager.activateInstalledPlugins();

        // record post-discovery state
        int customizersPost = mc.getCustomizers().size();
        int processorsPost = mc.getProcessors().size();

        // we had discovered some?
        assertTrue( "The map should grow!", customizersPre < customizersPost );
        assertTrue( "The map should grow!", processorsPre < processorsPost );

        // now destroy
        nexusPluginManager.deactivatePlugin( response.getProcessedPluginCoordinates().get( 0 ) );

        // record post-destroy state
        int customizersDestroy = mc.getCustomizers().size();
        int processorsDestroy = mc.getProcessors().size();

        // we had destroyed some?
        assertTrue( "The map should shrink!", customizersDestroy < customizersPost );
        assertTrue( "The map should shrink!", processorsDestroy < processorsPost );
        assertTrue( "The map should shrink!", customizersDestroy == customizersPre );
        assertTrue( "The map should shrink!", processorsDestroy == processorsPre );
    }

}
