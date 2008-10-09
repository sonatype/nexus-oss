package org.sonatype.nexus.integrationtests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sonatype.nexus.integrationtests.client.nexus725.Nexus725InitialRestClient;
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
import org.sonatype.nexus.integrationtests.nexus384.Nexus384DotAndDashSearchTest;
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
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudValidationTests;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudXMLTests;
import org.sonatype.nexus.integrationtests.nexus532.Nexus532GroupsCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskCronTest;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskManualTest;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskMonthlyTest;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskOnceTest;
import org.sonatype.nexus.integrationtests.nexus533.Nexus533TaskWeeklyTest;
import org.sonatype.nexus.integrationtests.nexus586.Nexus586AnonymousChangePasswordTest;
import org.sonatype.nexus.integrationtests.nexus586.Nexus586AnonymousForgotPasswordTest;
import org.sonatype.nexus.integrationtests.nexus586.Nexus586AnonymousForgotUserIdTest;
import org.sonatype.nexus.integrationtests.nexus586.Nexus586AnonymousResetPasswordTest;
import org.sonatype.nexus.integrationtests.nexus598.Nexus598ClassnameSearchTest;
import org.sonatype.nexus.integrationtests.nexus602.Nexus602SearchSnapshotArtifactTest;
import org.sonatype.nexus.integrationtests.nexus606.Nexus606DownloadLogsAndConfigFilesTest;
import org.sonatype.nexus.integrationtests.nexus634.Nexus634KeepNewSnapshotsTest;
import org.sonatype.nexus.integrationtests.nexus634.Nexus634KeepTwoSnapshotsTest;
import org.sonatype.nexus.integrationtests.nexus634.Nexus634RemoveAllTest;
import org.sonatype.nexus.integrationtests.nexus636.Nexus636EvictUnusedProxiedTaskTest;
import org.sonatype.nexus.integrationtests.nexus637.Nexus637PublishIndexTest;
import org.sonatype.nexus.integrationtests.nexus640.Nexus640RebuildRepositoryAttributesTaskTest;
import org.sonatype.nexus.integrationtests.nexus641.Nexus641ReindexTaskTest;
import org.sonatype.nexus.integrationtests.nexus642.Nexus642SynchShadowTaskTest;
import org.sonatype.nexus.integrationtests.nexus643.Nexus643EmptyTrashTaskTest;
import org.sonatype.nexus.integrationtests.nexus688.Nexus688ReindexOnRepoAdd;
import org.sonatype.nexus.integrationtests.nexus782.Nexus782UploadWithClassifier;
import org.sonatype.nexus.integrationtests.nexus810.Nexus810PackageNamesInNexusConf;
import org.sonatype.nexus.integrationtests.nexus810.Nexus810PackageNamesInRestMessages;
import org.sonatype.nexus.integrationtests.nexus947.Nexus947GroupBrowsing;
import org.sonatype.nexus.integrationtests.proxy.nexus177.Nexus177OutOfServiceTest;
import org.sonatype.nexus.integrationtests.proxy.nexus178.Nexus178BlockProxyDownloadTest;
import org.sonatype.nexus.integrationtests.proxy.nexus179.Nexus179RemoteRepoDownTest;
import org.sonatype.nexus.integrationtests.proxy.nexus262.Nexus262SimpleProxyTest;
import org.sonatype.nexus.integrationtests.proxy.nexus635.Nexus635ClearCacheTaskTest;
import org.sonatype.nexus.integrationtests.upgrades.nexus652.Nexus652Beta5To10UpgradeTest;

/**
 * 
 */
@RunWith( Suite.class )
@SuiteClasses( {
    Nexus166SampleTest.class,
    Nexus169ReleaseMetaDataInSnapshotRepoTest.class,
    Nexus258ReleaseDeployTest.class,
    Nexus167ReleaseToSnapshotTest.class,
    Nexus168SnapshotToReleaseTest.class,
    Nexus176DeployToInvalidRepoTest.class,
    Nexus259SnapshotDeployTest.class,
    Nexus260MultipleDeployTest.class,
    Nexus261NexusGroupDownloadTest.class,
    Nexus177OutOfServiceTest.class,
    Nexus178BlockProxyDownloadTest.class,
    Nexus179RemoteRepoDownTest.class,
    Nexus262SimpleProxyTest.class,
    Nexus292SoftRestartTest.class,
    Nexus133TargetCrudJsonTests.class,
    Nexus133TargetCrudXmlTests.class,
    Nexus142UserCrudJsonTests.class,
    Nexus142UserCrudXmlTests.class,
    Nexus156RolesCrudJsonTests.class,
    Nexus156RolesCrudXmlTests.class,
    Nexus142UserValidationTests.class,
    Nexus156RolesValidationTests.class,
    Nexus133TargetValidationTests.class,
    Nexus233PrivilegesCrudXMLTests.class,
    Nexus233PrivilegesValidationTests.class,
    Nexus393ResetPasswordTest.class,
    Nexus394ForgotPasswordTest.class,
    Nexus385RoutesCrudXmlTests.class,
    Nexus385RoutesValidationTests.class,
    Nexus387RoutesTests.class,
    Nexus395ForgotUsernameTest.class,
    Nexus408ChangePasswordTest.class,
    Nexus526FeedsTests.class,
    Nexus531RepositoryCrudXMLTests.class,
    Nexus531RepositoryCrudJsonTests.class,
    Nexus533TaskManualTest.class,
    Nexus533TaskOnceTest.class,
    Nexus533TaskWeeklyTest.class,
    Nexus533TaskMonthlyTest.class,
    Nexus533TaskCronTest.class,
    Nexus533TaskCronTest.class,
    Nexus233PrivilegesCrudXMLTests.class,
    Nexus379VirtualRepoSameId.class,
    Nexus448PrivilegeURLTest.class,
    Nexus586AnonymousChangePasswordTest.class,
    Nexus586AnonymousForgotPasswordTest.class,
    Nexus586AnonymousForgotUserIdTest.class,
    Nexus586AnonymousResetPasswordTest.class,
    Nexus532GroupsCrudXmlTests.class,
    Nexus606DownloadLogsAndConfigFilesTest.class,
    Nexus643EmptyTrashTaskTest.class,
    Nexus637PublishIndexTest.class,
    Nexus652Beta5To10UpgradeTest.class,
    Nexus602SearchSnapshotArtifactTest.class,
    Nexus635ClearCacheTaskTest.class,
    Nexus634RemoveAllTest.class,
    Nexus634KeepNewSnapshotsTest.class,
    Nexus634KeepTwoSnapshotsTest.class,
    Nexus636EvictUnusedProxiedTaskTest.class,
    Nexus598ClassnameSearchTest.class,
    Nexus640RebuildRepositoryAttributesTaskTest.class,
    Nexus531RepositoryCrudValidationTests.class,
    Nexus810PackageNamesInRestMessages.class,
    Nexus810PackageNamesInNexusConf.class,
    Nexus782UploadWithClassifier.class,
    Nexus688ReindexOnRepoAdd.class,
    Nexus384DotAndDashSearchTest.class,
    Nexus642SynchShadowTaskTest.class,
    Nexus947GroupBrowsing.class } )
public class IntegrationTestSuiteClasses
{

}
