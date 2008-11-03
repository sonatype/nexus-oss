package org.sonatype.nexus.integrationtests.client.nexus758;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.appbooter.AbstractForkedAppBooter;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.ServiceStatusUtil;
import org.sonatype.nexus.test.utils.TestProperties;

/**
 * Tests the Soft Start, Stop, Restart, and isNexusStarted methods in the rest-client.
 */
public class Nexus758StatusService
    extends AbstractNexusIntegrationTest
{

    private NexusClient getConnectedNexusClient()
        throws Exception
    {

        NexusClient client = (NexusClient) TestContainer.getInstance().lookup( NexusClient.ROLE );
        TestContext context = TestContainer.getInstance().getTestContext();
        client.connect( TestProperties.getString( "nexus.base.url" ), context.getAdminUsername(),
                        context.getAdminPassword() );

        return client;
    }

    @Test
    public void startAndStopTest()
        throws Exception
    {
        NexusClient client = this.getConnectedNexusClient();

        // stop Nexus
        client.stopNexus(); // blocking
        Assert.assertTrue( "Expected Nexus to be Stopped", ServiceStatusUtil.waitForStop( client ) );

        // start Nexus
        client.startNexus(); // blocking
        Assert.assertTrue( "Expected Nexus to be Started", ServiceStatusUtil.waitForStart( client ) );

        client.disconnect();
    }

    @Test
    public void restartTest()
        throws Exception
    {
        NexusClient client = this.getConnectedNexusClient();

        // restart Nexus
        client.restartNexus(); // this is blocking
        Assert.assertTrue( "Expected Nexus to be Started", client.isNexusStarted( false ) );

        client.disconnect();
    }

    @Test
    public void waitForStartTest()
        throws Exception
    {
        NexusClient client = null;

        AbstractForkedAppBooter appBooter =
            (AbstractForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );

        try
        {
            appBooter.stop();

            client = this.getConnectedNexusClient();
            // turn down the timeout to speed up the tests

            Assert.assertTrue( "Wait for start, timed out.", ServiceStatusUtil.waitForStop( client ) );

            appBooter.setSleepAfterStart( 0 );
            appBooter.start();
            // set the timeout back to 16 sec
            Assert.assertTrue( "Wait for start, timed out.", ServiceStatusUtil.waitForStart( client ) );

        }
        finally
        {
            // hack, but we needed to be able to test the timeout
            appBooter.setSleepAfterStart( 6000 );
        }

        client.disconnect();
    }

}
