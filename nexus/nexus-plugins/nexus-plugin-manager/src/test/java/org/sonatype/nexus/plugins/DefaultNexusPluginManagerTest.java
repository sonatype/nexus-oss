package org.sonatype.nexus.plugins;

public class DefaultNexusPluginManagerTest
    extends AbstractNexusPluginManagerTest
{
    public void testSimple()
        throws Exception
    {
        nexusPluginManager = (DefaultNexusPluginManager) lookup( NexusPluginManager.class );

        MockComponent mc = getContainer().lookup( MockComponent.class );

        // record pre-discovery state
        int customizersPre = mc.getCustomizers().size();
        int processorsPre = mc.getProcessors().size();

        // do discovery, both recorded value should be +1
        PluginManagerResponse pmresponse = nexusPluginManager.activateInstalledPlugins();

        assertEquals( "Two plugins should be discovered!", 2, pmresponse.getProcessedPluginResponses().size() );
        assertEquals( "Should be okay!", RequestResult.COMPLETELY_EXECUTED, pmresponse.getResult() );

        // record post-discovery state
        int customizersPost = mc.getCustomizers().size();
        int processorsPost = mc.getProcessors().size();

        // we had discovered some?
        assertTrue( "The map should grow!", customizersPre < customizersPost );
        assertTrue( "The map should grow!", processorsPre < processorsPost );

        // lookup the virus customizer from plugin
        Object virusScanner = lookup( "org.sonatype.nexus.plugin.samples.kungfu.VirusScanner", "XY" );
        assertNotNull( virusScanner );

        // lookup the collector from other plugin
        Object infectedFilesCollector = lookup( "org.sonatype.nexus.plugin.samples.interdep.InfectedFilesCollector" );
        assertNotNull( infectedFilesCollector );

        // lookup the collector from other plugin
        Object infectedFilesCollectorFeedSource =
            lookup( "org.sonatype.nexus.rest.feeds.sources.FeedSource",
                    "org.sonatype.nexus.plugin.samples.interdep.InfectedItemsFeedSource" );

        // now destroy
        PluginResponse presponse1 =
            nexusPluginManager.deactivatePlugin( pmresponse.getProcessedPluginCoordinates().get( 0 ) );
        PluginResponse presponse2 =
            nexusPluginManager.deactivatePlugin( pmresponse.getProcessedPluginCoordinates().get( 1 ) );

        assertEquals( "Should be succesful!", true, presponse1.isSuccesful() );
        assertEquals( "Should be succesful!", true, presponse2.isSuccesful() );

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
