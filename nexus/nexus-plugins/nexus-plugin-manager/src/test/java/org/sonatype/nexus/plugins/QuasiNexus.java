package org.sonatype.nexus.plugins;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

@Component( role = QuasiNexus.class )
public class QuasiNexus
    implements Initializable
{
    @Requirement
    private NexusPluginManager nexusPluginManager;

    @Requirement
    private MockComponent mc;

    public void initialize()
        throws InitializationException
    {
        // within Nexus, all is happening in initialize() method
        
        // record pre-discovery state
        int customizersPre = mc.getCustomizers().size();
        int processorsPre = mc.getProcessors().size();

        // do discovery, both recorded value should be +1
        PluginManagerResponse pmresponse = nexusPluginManager.activateInstalledPlugins();

        
        TestCase.assertEquals( "One plugin should be discovered!", 2, pmresponse.getProcessedPluginResponses().size() );
        TestCase.assertEquals( "Should be okay!", RequestResult.COMPLETELY_EXECUTED, pmresponse.getResult() );

        // record post-discovery state
        int customizersPost = mc.getCustomizers().size();
        int processorsPost = mc.getProcessors().size();

        // we had discovered some?
        TestCase.assertTrue( "The map should grow!", customizersPre < customizersPost );
        TestCase.assertTrue( "The map should grow!", processorsPre < processorsPost );

        // now destroy
        PluginResponse presponse =
            nexusPluginManager.deactivatePlugin( pmresponse.getProcessedPluginCoordinates().get( 0 ) );

        TestCase.assertEquals( "Should be succesful!", true, presponse.isSuccesful() );

        // record post-destroy state
        int customizersDestroy = mc.getCustomizers().size();
        int processorsDestroy = mc.getProcessors().size();

        // we had destroyed some?
        TestCase.assertTrue( "The map should shrink!", customizersDestroy < customizersPost );
        TestCase.assertTrue( "The map should shrink!", processorsDestroy < processorsPost );
        TestCase.assertTrue( "The map should shrink!", customizersDestroy == customizersPre );
        TestCase.assertTrue( "The map should shrink!", processorsDestroy == processorsPre );
    }

}
