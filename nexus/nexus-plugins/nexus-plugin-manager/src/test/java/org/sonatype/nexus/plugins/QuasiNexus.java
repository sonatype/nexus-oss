package org.sonatype.nexus.plugins;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

@Component( role = QuasiNexus.class )
public class QuasiNexus
    implements Initializable
{
    @Requirement
    private PlexusContainer plexusContainer;

    @Requirement( role = NexusPluginManager.class )
    private DefaultNexusPluginManager nexusPluginManager;

    @Requirement
    private MockComponent mc;

    public void initialize()
        throws InitializationException
    {
        // within Nexus, all is happening in initialize() method

        try
        {
            // record pre-discovery state
            int customizersPre = mc.getCustomizers().size();
            int processorsPre = mc.getProcessors().size();

            // do discovery, both recorded value should be +1
            PluginManagerResponse pmresponse = nexusPluginManager.activateInstalledPlugins();

            Assert.assertEquals( "Two plugins should be discovered!", 2, pmresponse.getProcessedPluginResponses()
                .size() );
            Assert.assertEquals( "Should be okay!", RequestResult.COMPLETELY_EXECUTED, pmresponse.getResult() );

            // record post-discovery state
            int customizersPost = mc.getCustomizers().size();
            int processorsPost = mc.getProcessors().size();

            // we had discovered some?
            Assert.assertTrue( "The map should grow!", customizersPre < customizersPost );
            Assert.assertTrue( "The map should grow!", processorsPre < processorsPost );

            // lookup the virus customizer from plugin
            Object virusScanner =
                plexusContainer.lookup( "org.sonatype.nexus.plugin.samples.kungfu.VirusScanner", "XY" );
            Assert.assertNotNull( virusScanner );

            // lookup the collector from other plugin
            Object infectedFilesCollector =
                plexusContainer.lookup( "org.sonatype.nexus.plugin.samples.interdep.InfectedFilesCollector" );
            Assert.assertNotNull( infectedFilesCollector );

            // lookup the collector from other plugin
            Object infectedFilesCollectorFeedSource =
                plexusContainer.lookup( "org.sonatype.nexus.rest.feeds.sources.FeedSource", "infectedItems" );
            Assert.assertNotNull( infectedFilesCollectorFeedSource );

            // now destroy
            PluginResponse presponse1 =
                nexusPluginManager.deactivatePlugin( pmresponse.getProcessedPluginCoordinates().get( 0 ) );
            PluginResponse presponse2 =
                nexusPluginManager.deactivatePlugin( pmresponse.getProcessedPluginCoordinates().get( 1 ) );

            Assert.assertEquals( "Should be succesful!", true, presponse1.isSuccesful() );
            Assert.assertEquals( "Should be succesful!", true, presponse2.isSuccesful() );

            // record post-destroy state
            int customizersDestroy = mc.getCustomizers().size();
            int processorsDestroy = mc.getProcessors().size();

            // we had destroyed some?
            Assert.assertTrue( "The map should shrink!", customizersDestroy < customizersPost );
            Assert.assertTrue( "The map should shrink!", processorsDestroy < processorsPost );
            Assert.assertTrue( "The map should shrink!", customizersDestroy == customizersPre );
            Assert.assertTrue( "The map should shrink!", processorsDestroy == processorsPre );
        }
        catch ( ComponentLookupException e )
        {
            throw new InitializationException( "Bad!", e );
        }
    }

}
