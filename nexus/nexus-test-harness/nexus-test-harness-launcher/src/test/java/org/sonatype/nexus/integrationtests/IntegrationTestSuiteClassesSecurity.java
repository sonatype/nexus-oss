package org.sonatype.nexus.integrationtests;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sonatype.nexus.integrationtests.client.nexus725.Nexus725InitialRestClient;
import org.sonatype.nexus.integrationtests.client.nexus758.Nexus758StatusService;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetPermissionTests;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetValidationTests;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserPermissionTests;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserValidationTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesPermissionTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesValidationTests;
import org.sonatype.nexus.integrationtests.nexus166.Nexus166SampleTest;
import org.sonatype.nexus.integrationtests.nexus167.Nexus167ReleaseToSnapshotTest;
import org.sonatype.nexus.integrationtests.nexus168.Nexus168SnapshotToReleaseTest;
import org.sonatype.nexus.integrationtests.nexus169.Nexus169ReleaseMetaDataInSnapshotRepoTest;
import org.sonatype.nexus.integrationtests.nexus174.Nexus174ReleaseDeployWrongPassword;
import org.sonatype.nexus.integrationtests.nexus175.Nexus175SnapshotDeployWrongPassword;
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
import org.sonatype.nexus.integrationtests.nexus385.Nexus385RoutesPermissionTests;
import org.sonatype.nexus.integrationtests.nexus385.Nexus385RoutesValidationTests;
import org.sonatype.nexus.integrationtests.nexus387.Nexus387RoutesTests;
import org.sonatype.nexus.integrationtests.nexus393.Nexus393ResetPasswordPermissionTest;
import org.sonatype.nexus.integrationtests.nexus394.Nexus394ForgotPasswordPermissionTest;
import org.sonatype.nexus.integrationtests.nexus395.Nexus395ForgotUsernamePermissionTest;
import org.sonatype.nexus.integrationtests.nexus408.Nexus408ChangePasswordPermissionTest;
import org.sonatype.nexus.integrationtests.nexus429.Nexus429UploadArtifactPrivilegeTest;
import org.sonatype.nexus.integrationtests.nexus429.Nexus429WagonDeployPrivilegeTest;
import org.sonatype.nexus.integrationtests.nexus448.Nexus448PrivilegeURLTest;
import org.sonatype.nexus.integrationtests.nexus450.Nexus450UserCreationTest;
import org.sonatype.nexus.integrationtests.nexus477.Nexus477ArtifactsCrudTests;
import org.sonatype.nexus.integrationtests.nexus502.Nexus502MavenExecutionTest;
import org.sonatype.nexus.integrationtests.nexus511.Nexus511MavenDeployTest;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudPermissionTests;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudValidationTests;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudXMLTests;
import org.sonatype.nexus.integrationtests.nexus532.Nexus532GroupCrudPermissionTests;
import org.sonatype.nexus.integrationtests.nexus532.Nexus532GroupsCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus586.Nexus586ValidateConfigurationTest;
import org.sonatype.nexus.integrationtests.nexus606.Nexus606DownloadLogsAndConfigFilesTest;
import org.sonatype.nexus.integrationtests.nexus650.Nexus650ChangePasswordAndRebootTest;
import org.sonatype.nexus.integrationtests.proxy.nexus177.Nexus177OutOfServiceTest;
import org.sonatype.nexus.integrationtests.proxy.nexus178.Nexus178BlockProxyDownloadTest;
import org.sonatype.nexus.integrationtests.proxy.nexus179.Nexus179RemoteRepoDownTest;
import org.sonatype.nexus.integrationtests.proxy.nexus262.Nexus262SimpleProxyTest;
import org.sonatype.nexus.integrationtests.upgrades.nexus652.Nexus652Beta5To10UpgradeTest;

@RunWith( Suite.class )
@SuiteClasses( { Nexus166SampleTest.class, Nexus169ReleaseMetaDataInSnapshotRepoTest.class,
    Nexus258ReleaseDeployTest.class, Nexus167ReleaseToSnapshotTest.class, Nexus168SnapshotToReleaseTest.class,
    Nexus176DeployToInvalidRepoTest.class, Nexus259SnapshotDeployTest.class, Nexus260MultipleDeployTest.class,
    Nexus261NexusGroupDownloadTest.class, Nexus177OutOfServiceTest.class, Nexus178BlockProxyDownloadTest.class,
    Nexus179RemoteRepoDownTest.class, Nexus262SimpleProxyTest.class, Nexus292SoftRestartTest.class,
    Nexus133TargetCrudJsonTests.class, Nexus133TargetCrudXmlTests.class, Nexus142UserCrudJsonTests.class,
    Nexus142UserCrudXmlTests.class, Nexus156RolesCrudJsonTests.class, Nexus156RolesCrudXmlTests.class,
    Nexus142UserValidationTests.class, Nexus156RolesValidationTests.class, Nexus133TargetValidationTests.class,
    Nexus233PrivilegesCrudXMLTests.class, Nexus233PrivilegesValidationTests.class, Nexus385RoutesCrudXmlTests.class,
    Nexus385RoutesValidationTests.class, Nexus387RoutesTests.class, Nexus429UploadArtifactPrivilegeTest.class,
    Nexus133TargetPermissionTests.class, Nexus142UserPermissionTests.class, Nexus156RolesPermissionTests.class,
    Nexus385RoutesPermissionTests.class, Nexus429WagonDeployPrivilegeTest.class,
    Nexus393ResetPasswordPermissionTest.class, Nexus394ForgotPasswordPermissionTest.class,
    Nexus385RoutesPermissionTests.class, Nexus395ForgotUsernamePermissionTest.class,
    Nexus408ChangePasswordPermissionTest.class, Nexus450UserCreationTest.class, Nexus502MavenExecutionTest.class,
    Nexus477ArtifactsCrudTests.class, Nexus174ReleaseDeployWrongPassword.class,
    Nexus175SnapshotDeployWrongPassword.class, Nexus511MavenDeployTest.class, Nexus531RepositoryCrudXMLTests.class,
    Nexus531RepositoryCrudJsonTests.class, Nexus233PrivilegesCrudXMLTests.class, Nexus379VirtualRepoSameId.class,
    Nexus448PrivilegeURLTest.class, Nexus532GroupsCrudXmlTests.class, Nexus586ValidateConfigurationTest.class,
    Nexus606DownloadLogsAndConfigFilesTest.class, Nexus652Beta5To10UpgradeTest.class,
    Nexus650ChangePasswordAndRebootTest.class, Nexus725InitialRestClient.class,
    Nexus531RepositoryCrudValidationTests.class, Nexus531RepositoryCrudPermissionTests.class,
    Nexus532GroupCrudPermissionTests.class, Nexus758StatusService.class } )
public class IntegrationTestSuiteClassesSecurity
{
    @BeforeClass
    public static void beforeSuite()
        throws Exception
    {

        System.out.println( "\n\n\n****************************\n   Running Security Tests   \n****************************\n\n\n" );

        // enable security
        TestContainer.getInstance().getTestContext().setSecureTest( true );

    }
}
