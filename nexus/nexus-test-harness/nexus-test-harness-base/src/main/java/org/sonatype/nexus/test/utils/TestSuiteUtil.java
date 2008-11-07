package org.sonatype.nexus.test.utils;

import java.io.File;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.rest.NexusRestClient;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;

public class TestSuiteUtil
{

    protected static Logger log = Logger.getLogger( TestSuiteUtil.class );

    public static void stopNexus()
        throws Exception
    {
        log.info( "Doing hard stop" );

        ForkedAppBooter appBooter =
            (ForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );
        appBooter.shutdown();
    }

    public static void startNexus()
        throws Exception
    {
        log.info( "Doing hard start" );

        // checkPreviousInstance(); r1931 no longer required, port allocator put each new instance in a new port

        TestContainer testContainer = TestContainer.getInstance();

        // copy default nexus.xml
        File testConfigFile = AbstractNexusIntegrationTest.getResource( "default-config/nexus.xml" );
        File outputFile =
            new File( AbstractNexusIntegrationTest.nexusBaseDir + "/"
                + AbstractNexusIntegrationTest.RELATIVE_WORK_CONF_DIR, "nexus.xml" );
        FileTestingUtils.fileCopy( testConfigFile, outputFile );

        // start nexus
        ForkedAppBooter appBooter =
            (ForkedAppBooter) testContainer.lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );
        appBooter.start();

        // use the java client to wait for nexus to start
        // the ForkedAppBooter waits 6 seconds, and that is configurable
        // The client will wait 10 seconds by default, but will poll every half second.
        NexusClient client = new NexusRestClient();
        // at this point security should not be turned on, but you never know...
        client.connect( TestProperties.getString( "nexus.base.url" ),
                        testContainer.getTestContext().getAdminUsername(),
                        testContainer.getTestContext().getAdminPassword() );

        Assert.assertTrue( "Unable to start nexus after 80 seconds", ServiceStatusUtil.waitForStart( client ) );

        // now to make everything work correctly we actually have to "soft-stop" nexus
        NexusStateUtil.doSoftStop();

        Assert.assertTrue( ServiceStatusUtil.waitForStop( client ) );
        client.disconnect();
    }
}
