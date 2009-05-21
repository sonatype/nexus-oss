package org.sonatype.nexus.seleniumtests.nexus1816;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.appbooter.DefaultForkedAppBooter;
import org.sonatype.appbooter.ForkedAppBooter;

public class ManualStartNexus
    extends PlexusTestCase
{

    @Override
    protected String getCustomConfigurationName()
    {
        return "/META-INF/plexus/components.xml";
    }

    public void testStart() throws Exception
    {
        
        DefaultForkedAppBooter appBooter = (DefaultForkedAppBooter) this.lookup( ForkedAppBooter.ROLE,
        "TestForkedAppBooter" );
        appBooter.setDebug( true );
//        appBooter.setDebugPort( 5005 ); // the default
        appBooter.start();
        
        Thread.sleep( 999999999 );
    }
    
//    public void testStop() throws Exception
//    {   
//        DefaultForkedAppBooter appBooter = (DefaultForkedAppBooter) this.lookup( ForkedAppBooter.ROLE,
//        "TestForkedAppBooter" );
//        appBooter.stop();
//    }
}
