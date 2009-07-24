package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusContainer;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;

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
        
        RepositoryTypeRegistry repoTypeRegistry = getContainer().lookup( RepositoryTypeRegistry.class );

        // record pre-discovery state
        int repoTypeCountPre = repoTypeRegistry.getRegisteredRepositoryTypeDescriptors().size();
        int customizersPre = mc.getCustomizers().size();
        int processorsPre = mc.getProcessors().size();

        // do discovery, both recorded value should be +1
        Collection<PluginManagerResponse> activationResponses = nexusPluginManager.activateInstalledPlugins();

        Assert.assertEquals( "Three plugins should be discovered!", 4, activationResponses.size() );

        for ( PluginManagerResponse response : activationResponses )
        {
            System.out.println( response.formatAsString( true ) );

            Assert.assertEquals( "Should be okay!", true, response.isSuccessful() );
        }

        // record post-discovery state
        int repoTypeCountPost = repoTypeRegistry.getRegisteredRepositoryTypeDescriptors().size();
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
        List<PluginDescriptor> activePlugins =
            new ArrayList<PluginDescriptor>( nexusPluginManager.getActivatedPlugins().values() );

        for ( PluginDescriptor pd : activePlugins )
        {
            PluginManagerResponse response = nexusPluginManager.deactivatePlugin( pd.getPluginCoordinates() );

            Assert.assertEquals( "Should be succesful!", true, response.isSuccessful() );
        }

        // record post-destroy state
        int repoTypeCountDestroy = repoTypeRegistry.getRegisteredRepositoryTypeDescriptors().size();
        int customizersDestroy = mc.getCustomizers().size();
        int processorsDestroy = mc.getProcessors().size();

        // we had destroyed some?
        Assert.assertTrue( "The repo types should be registered!", repoTypeCountPre < repoTypeCountPost );
        Assert.assertTrue( "The repo types should be deregistered!", repoTypeCountDestroy < repoTypeCountPost );
        Assert.assertTrue( "The map should shrink!", customizersDestroy < customizersPost );
        Assert.assertTrue( "The map should shrink!", processorsDestroy < processorsPost );
        Assert.assertTrue( "The map should shrink!", customizersDestroy == customizersPre );
        Assert.assertTrue( "The map should shrink!", processorsDestroy == processorsPre );
    }
}
