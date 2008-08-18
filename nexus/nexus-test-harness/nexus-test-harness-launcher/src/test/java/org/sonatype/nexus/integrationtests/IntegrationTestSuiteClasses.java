package org.sonatype.nexus.integrationtests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetValidationTests;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserValidationTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesValidationTests;
import org.sonatype.nexus.integrationtests.nexus166.Nexus166SampleTest;
import org.sonatype.nexus.integrationtests.nexus167.Nexus167ReleaseToSnapshotTest;
import org.sonatype.nexus.integrationtests.nexus168.Nexus168SnapshotToReleaseTest;
import org.sonatype.nexus.integrationtests.nexus169.Nexus169ReleaseMetaDataInSnapshotRepoTest;
import org.sonatype.nexus.integrationtests.nexus176.Nexus176DeployToInvalidRepoTest;
import org.sonatype.nexus.integrationtests.nexus233.Nexus233PrivilegesCrudXMLTests;
import org.sonatype.nexus.integrationtests.nexus233.Nexus233PrivilegesValidationTests;
import org.sonatype.nexus.integrationtests.nexus258.Nexus258ReleaseDeployTest;
import org.sonatype.nexus.integrationtests.nexus259.Nexus259SnapshotDeployTest;
import org.sonatype.nexus.integrationtests.nexus260.Nexus260MultipleDeployTest;
import org.sonatype.nexus.integrationtests.nexus261.Nexus261NexusGroupDownloadTest;
import org.sonatype.nexus.integrationtests.nexus292.Nexus292SoftRestartTest;
import org.sonatype.nexus.integrationtests.nexus379.Nexus379VirtualRepoSameId;
import org.sonatype.nexus.integrationtests.nexus385.Nexus385RoutesCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus385.Nexus385RoutesValidationTests;
import org.sonatype.nexus.integrationtests.nexus387.Nexus387RoutesTests;
import org.sonatype.nexus.integrationtests.nexus393.Nexus393ResetPasswordTest;
import org.sonatype.nexus.integrationtests.nexus394.Nexus394ForgotPasswordTest;
import org.sonatype.nexus.integrationtests.nexus395.Nexus395ForgotUsernameTest;
import org.sonatype.nexus.integrationtests.nexus408.Nexus408ChangePasswordTest;
import org.sonatype.nexus.integrationtests.nexus448.Nexus448PrivilegeURLTest;
import org.sonatype.nexus.integrationtests.nexus526.Nexus526FeedsTests;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudXMLTests;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskCronTest;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskManualTest;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskMonthlyTest;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskOnceTest;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskWeeklyTest;
import org.sonatype.nexus.integrationtests.proxy.nexus177.Nexus177OutOfServiceTest;
import org.sonatype.nexus.integrationtests.proxy.nexus178.Nexus178BlockProxyDownloadTest;
import org.sonatype.nexus.integrationtests.proxy.nexus179.Nexus179RemoteRepoDownTest;
import org.sonatype.nexus.integrationtests.proxy.nexus262.Nexus262SimpleProxyTest;

/**
 * NOTE: the class Nexus258ReleaseDeployTest needs to be at the start of the test, something happens, ( I don't know
 * what yet) if its not near, the top of this list it will fail to deploy its artifacts. It doesn't seem to be a timing
 * issue. And its not a work dir problem, any thoughts, I am all ears! I think i fixed the above problem, but i am going
 * to leave this note here untill i am sure.
 */
@RunWith( Suite.class )
@SuiteClasses( { Nexus166SampleTest.class, Nexus169ReleaseMetaDataInSnapshotRepoTest.class,
    Nexus258ReleaseDeployTest.class, Nexus167ReleaseToSnapshotTest.class, Nexus168SnapshotToReleaseTest.class,
    Nexus176DeployToInvalidRepoTest.class, Nexus259SnapshotDeployTest.class, Nexus260MultipleDeployTest.class,
    Nexus261NexusGroupDownloadTest.class, Nexus177OutOfServiceTest.class, Nexus178BlockProxyDownloadTest.class,
    Nexus179RemoteRepoDownTest.class, Nexus262SimpleProxyTest.class, Nexus292SoftRestartTest.class,
    Nexus133TargetCrudJsonTests.class, Nexus133TargetCrudXmlTests.class, Nexus142UserCrudJsonTests.class,
    Nexus142UserCrudXmlTests.class, Nexus156RolesCrudJsonTests.class, Nexus156RolesCrudXmlTests.class,
    Nexus142UserValidationTests.class, Nexus156RolesValidationTests.class, Nexus133TargetValidationTests.class,
    Nexus233PrivilegesCrudXMLTests.class, Nexus233PrivilegesValidationTests.class, Nexus393ResetPasswordTest.class,
    Nexus394ForgotPasswordTest.class, Nexus385RoutesCrudXmlTests.class, Nexus385RoutesValidationTests.class,
    Nexus387RoutesTests.class, Nexus395ForgotUsernameTest.class, Nexus408ChangePasswordTest.class,
    Nexus526FeedsTests.class, Nexus531RepositoryCrudXMLTests.class, Nexus531RepositoryCrudJsonTests.class,
    Nexus533TaskManualTest.class, Nexus533TaskOnceTest.class, Nexus533TaskWeeklyTest.class,
    Nexus533TaskMonthlyTest.class, Nexus533TaskCronTest.class, Nexus233PrivilegesCrudXMLTests.class,
    Nexus379VirtualRepoSameId.class, Nexus448PrivilegeURLTest.class } )
public class IntegrationTestSuiteClasses
{

}
