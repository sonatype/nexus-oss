package org.sonatype.nexus.plugins;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusContainer;

public class Assertions
{
    private PlexusContainer plexusContainer;

    public Assertions( PlexusContainer plexusContainer )
    {
        this.plexusContainer = plexusContainer;
    }

    protected PlexusContainer getContainer()
    {
        return plexusContainer;
    }

    public void doCheck()
        throws Exception
    {
        DefaultNexusPluginManager nexusPluginManager =
            (DefaultNexusPluginManager) getContainer().lookup( NexusPluginManager.class );

        MockComponent mc = getContainer().lookup( MockComponent.class );

        // record pre-discovery state
        int customizersPre = mc.getCustomizers().size();
        int processorsPre = mc.getProcessors().size();

        // do discovery, both recorded value should be +1
        PluginManagerResponse pmresponse = nexusPluginManager.activateInstalledPlugins();

        System.out.println( pmresponse.formatAsString( true ) );

        Assert.assertEquals( "Three plugins should be discovered!", 3, pmresponse.getProcessedPluginResponses().size() );
        Assert.assertEquals( "Should be okay!", RequestResult.COMPLETED, pmresponse.getResult() );

        // record post-discovery state
        int customizersPost = mc.getCustomizers().size();
        int processorsPost = mc.getProcessors().size();

        // we had discovered some?
        Assert.assertTrue( "The map should grow!", customizersPre < customizersPost );
        Assert.assertTrue( "The map should grow!", processorsPre < processorsPost );

        // lookup the virus customizer from plugin
        Object virusScanner = getContainer().lookup( "org.sonatype.nexus.plugin.samples.kungfu.VirusScanner", "XY" );
        Assert.assertNotNull( virusScanner );

        // lookup the collector from other plugin
        Object infectedFilesCollector =
            getContainer().lookup( "org.sonatype.nexus.plugin.samples.interdep.InfectedFilesCollector" );
        Assert.assertNotNull( infectedFilesCollector );

        // lookup the collector from other plugin
        Object infectedFilesCollectorFeedSource =
            getContainer().lookup( "org.sonatype.nexus.rest.feeds.sources.FeedSource", "infectedItems" );
        Assert.assertNotNull( infectedFilesCollectorFeedSource );

        // now destroy
        PluginResponse presponse1 =
            nexusPluginManager.deactivatePlugin( pmresponse.getProcessedPluginCoordinates().get( 0 ) );
        PluginResponse presponse2 =
            nexusPluginManager.deactivatePlugin( pmresponse.getProcessedPluginCoordinates().get( 1 ) );
        PluginResponse presponse3 =
            nexusPluginManager.deactivatePlugin( pmresponse.getProcessedPluginCoordinates().get( 2 ) );

        Assert.assertEquals( "Should be succesful!", true, presponse1.isSuccesful() );
        Assert.assertEquals( "Should be succesful!", true, presponse2.isSuccesful() );
        Assert.assertEquals( "Should be succesful!", true, presponse3.isSuccesful() );

        // record post-destroy state
        int customizersDestroy = mc.getCustomizers().size();
        int processorsDestroy = mc.getProcessors().size();

        // we had destroyed some?
        Assert.assertTrue( "The map should shrink!", customizersDestroy < customizersPost );
        Assert.assertTrue( "The map should shrink!", processorsDestroy < processorsPost );
        Assert.assertTrue( "The map should shrink!", customizersDestroy == customizersPre );
        Assert.assertTrue( "The map should shrink!", processorsDestroy == processorsPre );
    }
}
