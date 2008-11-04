package org.sonatype.nexus.integrationtests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.rest.NexusRestClient;
import org.sonatype.nexus.test.utils.NexusStateUtil;
import org.sonatype.nexus.test.utils.TestProperties;

@RunWith( Suite.class )
@SuiteClasses( { IntegrationTestSuiteClasses.class, IntegrationTestSuiteClassesSecurity.class } )
public class IntegrationTestSuite
{
    //

    @BeforeClass
    public static void beforeSuite()
        throws Exception
    {
        // checkPreviousInstance(); r1931 no longer required, port allocator put each new instance in a new port

        TestContainer testContainer = TestContainer.getInstance();

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
        client.isNexusStarted( true );
        client.disconnect();

        // now to make everything work correctly we actually have to "soft-stop" nexus
//        NexusStateUtil.doSoftStop();

    }

    @AfterClass
    public static void afterSuite()
        throws Exception
    {
        ForkedAppBooter appBooter =
            (ForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );
        appBooter.shutdown();
    }

}
