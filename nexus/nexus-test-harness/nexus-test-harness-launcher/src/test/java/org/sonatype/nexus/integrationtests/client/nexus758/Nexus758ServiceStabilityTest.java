package org.sonatype.nexus.integrationtests.client.nexus758;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.appbooter.AbstractForkedAppBooter;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.ServiceStatusUtil;
import org.sonatype.nexus.test.utils.TestProperties;

/**
 * Tests the Soft Start, Stop, Restart, and isNexusStarted methods in the rest-client.
 */
public class Nexus758ServiceStabilityTest
{

    private static NexusClient client;

    @BeforeClass
    public static void init()
        throws Exception
    {
        client = (NexusClient) TestContainer.getInstance().lookup( NexusClient.ROLE );
        TestContext context = TestContainer.getInstance().getTestContext();
        client.connect( TestProperties.getString( "nexus.base.url" ), context.getAdminUsername(),
                        context.getAdminPassword() );
    }

    @Test
    public void hardRestarts()
        throws Exception
    {

        ForkedAppBooter app;

        // this could be done using a for, but I wanna to know how may times it run just looking to stack trace
        // 1
        app = hardStartTest();
        hardStopTest( app );

        // 2
        app = hardStartTest();
        hardStopTest( app );

        // 3
        app = hardStartTest();
        hardStopTest( app );

        // 4
        app = hardStartTest();
        hardStopTest( app );

        // 5
        app = hardStartTest();
        hardStopTest( app );

        // 6
        app = hardStartTest();
        hardStopTest( app );

        // 7
        app = hardStartTest();
        hardStopTest( app );

        // 8
        app = hardStartTest();
        hardStopTest( app );

        // 9
        app = hardStartTest();
        hardStopTest( app );

        // 10
        app = hardStartTest();
        hardStopTest( app );

    }

    public void startAndStopTest()
        throws Exception
    {
        // stop Nexus
        client.stopNexus(); // blocking
        Assert.assertTrue( "Expected Nexus to be Stopped", ServiceStatusUtil.waitForStop( client ) );

        // start Nexus
        client.startNexus(); // blocking
        Assert.assertTrue( "Expected Nexus to be Started", ServiceStatusUtil.waitForStart( client ) );

        client.disconnect();
    }

    public void restartTest()
        throws Exception
    {
        // restart Nexus
        client.restartNexus(); // this is blocking
        Assert.assertTrue( "Expected Nexus to be Started", client.isNexusStarted( false ) );

        client.disconnect();
    }

    public ForkedAppBooter hardStartTest()
        throws Exception
    {
        AbstractForkedAppBooter appBooter =
            (AbstractForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );

        Assert.assertFalse( "Nexus should not be started.", client.isNexusStarted( true ) );

        appBooter.setSleepAfterStart( 0 );
        appBooter.start();

        Assert.assertTrue( "Unable to start Nexus after 20 seconds", ServiceStatusUtil.waitForStart( client ) );

        return appBooter;
    }

    public void hardStopTest( ForkedAppBooter app )
        throws Exception
    {
        Assert.assertTrue( "Nexus is not started.", client.isNexusStarted( true ) );

        app.stop();

        Assert.assertTrue( "Unable to stop Nexus after 10 seconds", ServiceStatusUtil.waitForStop( client ) );
    }

}
