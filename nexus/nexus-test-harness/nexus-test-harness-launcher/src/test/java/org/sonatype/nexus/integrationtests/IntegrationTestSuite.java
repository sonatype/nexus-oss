package org.sonatype.nexus.integrationtests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.test.utils.NexusStateUtil;


@RunWith( Suite.class )
@SuiteClasses( { IntegrationTestSuiteClasses.class, IntegrationTestSuiteClassesSecurity.class } )
public class IntegrationTestSuite
{
    //

    @BeforeClass
    public static void beforeSuite()
        throws Exception
    {
        ForkedAppBooter appBooter =
            (ForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );
        appBooter.start();

        // now to make everything work correctly we actually have to "soft-stop" nexus
        NexusStateUtil.doSoftStop();

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
