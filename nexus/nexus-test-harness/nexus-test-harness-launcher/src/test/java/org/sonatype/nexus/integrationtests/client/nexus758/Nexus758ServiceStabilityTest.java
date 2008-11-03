package org.sonatype.nexus.integrationtests.client.nexus758;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.appbooter.AbstractForkedAppBooter;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.NexusStateUtil;
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
        app = doHardStart();
        doHardStop( app );

        // 2
        app = doHardStart();
        doHardStop( app );

        // 3
        app = doHardStart();
        doHardStop( app );

        // 4
        app = doHardStart();
        doHardStop( app );

        // 5
        app = doHardStart();
        doHardStop( app );

        // 6
        app = doHardStart();
        doHardStop( app );

        // 7
        app = doHardStart();
        doHardStop( app );

        // 8
        app = doHardStart();
        doHardStop( app );

        // 9
        app = doHardStart();
        doHardStop( app );

        // 10
        app = doHardStart();
        doHardStop( app );

    }

    @Test
    public void softRestarts()
        throws Exception
    {
        ForkedAppBooter app = doHardStart();

        doSoftStop();

        // 1
        doSoftStart();
        doSoftStop();

        // 2
        doSoftStart();
        doSoftStop();

        // 3
        doSoftStart();
        doSoftStop();

        // 4
        doSoftStart();
        doSoftStop();

        // 5
        doSoftStart();
        doSoftStop();

        // 6
        doSoftStart();
        doSoftStop();

        // 7
        doSoftStart();
        doSoftStop();

        // 8
        doSoftStart();
        doSoftStop();

        // 9
        doSoftStart();
        doSoftStop();

        // 10
        doSoftStart();
        doSoftStop();

        doSoftStart();
        doHardStop( app );
    }

    @Test
    public void clientRestarts()
        throws Exception
    {
        ForkedAppBooter app = doHardStart();

        doClientStop();

        // 1
        doClientStart();
        doClientStop();

        // 2
        doClientStart();
        doClientStop();

        // 3
        doClientStart();
        doClientStop();

        // 4
        doClientStart();
        doClientStop();

        // 5
        doClientStart();
        doClientStop();

        // 6
        doClientStart();
        doClientStop();

        // 7
        doClientStart();
        doClientStop();

        // 8
        doClientStart();
        doClientStop();

        // 9
        doClientStart();
        doClientStop();

        // 10
        doClientStart();
        doClientStop();

        doClientStart();
        doHardStop( app );
    }

    private void doClientStart()
        throws Exception
    {
        Assert.assertFalse( "Nexus should not be started.", client.isNexusStarted( true ) );

        client.startNexus();

        Assert.assertTrue( "Unable to start Nexus after 4 minutes", ServiceStatusUtil.waitForStart( client ) );
    }

    private void doClientStop()
        throws Exception
    {
        Assert.assertTrue( "Nexus is not started.", client.isNexusStarted( true ) );

        client.stopNexus();

        Assert.assertTrue( "Unable to stop Nexus after 4 minutes", ServiceStatusUtil.waitForStop( client ) );
    }

    private void doSoftStart()
        throws Exception
    {
        Assert.assertFalse( "Nexus should not be started.", client.isNexusStarted( true ) );

        NexusStateUtil.doSoftStart();

        Assert.assertTrue( "Unable to start Nexus after 4 minutes", ServiceStatusUtil.waitForStart( client ) );
    }

    private void doSoftStop()
        throws Exception
    {
        Assert.assertTrue( "Nexus is not started.", client.isNexusStarted( true ) );

        NexusStateUtil.doSoftStop();

        Assert.assertTrue( "Unable to stop Nexus after 4 minutes", ServiceStatusUtil.waitForStop( client ) );
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

    public ForkedAppBooter doHardStart()
        throws Exception
    {
        AbstractForkedAppBooter appBooter =
            (AbstractForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );

        Assert.assertFalse( "Nexus should not be started.", client.isNexusStarted( true ) );

        appBooter.setSleepAfterStart( 0 );
        appBooter.start();

        Assert.assertTrue( "Unable to start Nexus after 4 minutes", ServiceStatusUtil.waitForStart( client ) );

        return appBooter;
    }

    public void doHardStop( ForkedAppBooter app )
        throws Exception
    {
        Assert.assertTrue( "Nexus is not started.", client.isNexusStarted( true ) );

        app.stop();

        Assert.assertTrue( "Unable to stop Nexus after 4 minutes", ServiceStatusUtil.waitForStop( client ) );
    }

}
