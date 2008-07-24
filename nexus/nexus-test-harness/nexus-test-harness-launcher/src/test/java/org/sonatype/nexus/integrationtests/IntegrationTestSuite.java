package org.sonatype.nexus.integrationtests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus166.Nexus166SampleTest;
import org.sonatype.nexus.integrationtests.nexus167.Nexus167ReleaseToSnapshotTest;
import org.sonatype.nexus.integrationtests.nexus168.Nexus168SnapshotToReleaseTest;
import org.sonatype.nexus.integrationtests.nexus169.Nexus169ReleaseMetaDataInSnapshotRepoTest;
import org.sonatype.nexus.integrationtests.nexus176.Nexus176DeployToInvalidRepoTest;
import org.sonatype.nexus.integrationtests.nexus258.Nexus258ReleaseDeployTest;
import org.sonatype.nexus.integrationtests.nexus259.Nexus259SnapshotDeployTest;
import org.sonatype.nexus.integrationtests.nexus260.Nexus260MultipleDeployTest;
import org.sonatype.nexus.integrationtests.nexus261.Nexus261NexusGroupDownloadTest;
import org.sonatype.nexus.integrationtests.nexus292.Nexus292SoftRestartTest;
import org.sonatype.nexus.integrationtests.proxy.nexus177.Nexus177OutOfServiceTest;
import org.sonatype.nexus.integrationtests.proxy.nexus178.Nexus178BlockProxyDownloadTest;
import org.sonatype.nexus.integrationtests.proxy.nexus179.Nexus179RemoteRepoDownTest;
import org.sonatype.nexus.integrationtests.proxy.nexus262.Nexus262SimpleProxyTest;

/**
 * NOTE: the class Nexus258ReleaseDeployTest needs to be at the start of the test, something happens, ( I don't know
 * what yet) if its not near, the top of this list it will fail to deploy its artifacts. It doesn't seem to be a timing
 * issue. And its not a work dir problem, any thoughts, I am all ears!
 */
@RunWith( Suite.class )
@SuiteClasses( { Nexus258ReleaseDeployTest.class, Nexus169ReleaseMetaDataInSnapshotRepoTest.class,
    Nexus166SampleTest.class, Nexus167ReleaseToSnapshotTest.class, Nexus168SnapshotToReleaseTest.class,
    Nexus176DeployToInvalidRepoTest.class, Nexus259SnapshotDeployTest.class, Nexus260MultipleDeployTest.class,
    Nexus261NexusGroupDownloadTest.class, Nexus177OutOfServiceTest.class, Nexus178BlockProxyDownloadTest.class,
    Nexus179RemoteRepoDownTest.class, Nexus262SimpleProxyTest.class, Nexus292SoftRestartTest.class,
    Nexus133TargetCrudJsonTests.class, Nexus133TargetCrudXmlTests.class } )
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
