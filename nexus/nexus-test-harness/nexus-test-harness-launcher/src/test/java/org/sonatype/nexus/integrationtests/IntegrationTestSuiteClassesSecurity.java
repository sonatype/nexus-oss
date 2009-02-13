/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sonatype.nexus.integrationtests.client.nexus725.Nexus725InitialRestClient;
import org.sonatype.nexus.integrationtests.client.nexus758.Nexus758StatusService;
import org.sonatype.nexus.integrationtests.nexus1170.Nexus1170ReducePermissionChecking;
import org.sonatype.nexus.integrationtests.nexus1239.Nexus1239PlexusUserResourceTest;
import org.sonatype.nexus.integrationtests.nexus1239.Nexus1239UserSearchTest;
import org.sonatype.nexus.integrationtests.nexus1240.Nexus1240SourceInLoginResourceTest;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetPermissionTests;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetValidationTests;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserPermissionTests;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserValidationTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesPermissionTests;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesValidationTests;
import org.sonatype.nexus.integrationtests.nexus1592.Nexus1592ViewPrivTest;
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
import org.sonatype.nexus.integrationtests.nexus384.Nexus384DotAndDashSearchTest;
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
import org.sonatype.nexus.integrationtests.nexus504.Nexus504ChangeRoleTest;
import org.sonatype.nexus.integrationtests.nexus511.Nexus511MavenDeployTest;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudJsonTests;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudPermissionTests;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudValidationTests;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudXMLTests;
import org.sonatype.nexus.integrationtests.nexus532.Nexus532GroupCrudPermissionTests;
import org.sonatype.nexus.integrationtests.nexus532.Nexus532GroupsCrudXmlTests;
import org.sonatype.nexus.integrationtests.nexus537.Nexus537RepoTargetsTests;
import org.sonatype.nexus.integrationtests.nexus570.Nexus570IndexArchetypeTest;
import org.sonatype.nexus.integrationtests.nexus586.Nexus586ValidateConfigurationTest;
import org.sonatype.nexus.integrationtests.nexus606.Nexus606DownloadLogsAndConfigFilesTest;
import org.sonatype.nexus.integrationtests.nexus642.Nexus642SynchShadowTaskTest;
import org.sonatype.nexus.integrationtests.nexus650.Nexus650ChangePasswordAndRebootTest;
import org.sonatype.nexus.integrationtests.nexus688.Nexus688ReindexOnRepoAdd;
import org.sonatype.nexus.integrationtests.nexus778.Nexus778SearchResultsFilteringTest;
import org.sonatype.nexus.integrationtests.nexus779.Nexus779RssFeedFilteringTest;
import org.sonatype.nexus.integrationtests.nexus782.Nexus782UploadWithClassifier;
import org.sonatype.nexus.integrationtests.nexus810.Nexus810PackageNamesInNexusConf;
import org.sonatype.nexus.integrationtests.nexus810.Nexus810PackageNamesInRestMessages;
import org.sonatype.nexus.integrationtests.nexus930.Nexus930AutoDiscoverComponent;
import org.sonatype.nexus.integrationtests.nexus947.Nexus947GroupBrowsing;
import org.sonatype.nexus.integrationtests.nexus999.Nexus999SetUsersPassword;
import org.sonatype.nexus.integrationtests.proxy.nexus177.Nexus177OutOfServiceTest;
import org.sonatype.nexus.integrationtests.proxy.nexus178.Nexus178BlockProxyDownloadTest;
import org.sonatype.nexus.integrationtests.proxy.nexus179.Nexus179RemoteRepoDownTest;
import org.sonatype.nexus.integrationtests.proxy.nexus262.Nexus262SimpleProxyTest;
import org.sonatype.nexus.integrationtests.upgrades.nexus652.Nexus652Beta5To10UpgradeTest;

@RunWith( Suite.class )
@SuiteClasses( {
    Nexus166SampleTest.class,
    Nexus1170ReducePermissionChecking.class,
    Nexus758StatusService.class,
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
    /*
      Depends on https://issues.sonatype.org/browse/NEXUS-1302

      Nexus142UserCrudJsonTests.class,
      Nexus142UserCrudXmlTests.class,
     */
    Nexus156RolesCrudJsonTests.class,
    Nexus156RolesCrudXmlTests.class,
    Nexus142UserValidationTests.class,
    Nexus156RolesValidationTests.class,
    Nexus133TargetValidationTests.class,
    Nexus233PrivilegesCrudXMLTests.class,
    Nexus233PrivilegesValidationTests.class,
    Nexus385RoutesCrudXmlTests.class,
    Nexus385RoutesValidationTests.class,
    Nexus387RoutesTests.class,
    Nexus429UploadArtifactPrivilegeTest.class,
    Nexus133TargetPermissionTests.class,
    Nexus142UserPermissionTests.class,
    Nexus156RolesPermissionTests.class,
    Nexus385RoutesPermissionTests.class,
    Nexus429WagonDeployPrivilegeTest.class,
    Nexus393ResetPasswordPermissionTest.class,
    Nexus394ForgotPasswordPermissionTest.class,
    Nexus385RoutesPermissionTests.class,
    Nexus395ForgotUsernamePermissionTest.class,
    Nexus408ChangePasswordPermissionTest.class,
    Nexus450UserCreationTest.class,
    Nexus502MavenExecutionTest.class,
    Nexus477ArtifactsCrudTests.class,
    Nexus174ReleaseDeployWrongPassword.class,
    Nexus175SnapshotDeployWrongPassword.class,
    Nexus511MavenDeployTest.class,
    Nexus531RepositoryCrudXMLTests.class,
    Nexus531RepositoryCrudJsonTests.class,
    Nexus233PrivilegesCrudXMLTests.class,
    Nexus379VirtualRepoSameId.class,
    Nexus448PrivilegeURLTest.class,
    Nexus532GroupsCrudXmlTests.class,
    Nexus586ValidateConfigurationTest.class,
    Nexus606DownloadLogsAndConfigFilesTest.class,
    Nexus652Beta5To10UpgradeTest.class,
    Nexus650ChangePasswordAndRebootTest.class,
    Nexus725InitialRestClient.class,
    Nexus531RepositoryCrudValidationTests.class,
    Nexus531RepositoryCrudPermissionTests.class,
    Nexus532GroupCrudPermissionTests.class,
    Nexus810PackageNamesInRestMessages.class,
    Nexus810PackageNamesInNexusConf.class,
    Nexus782UploadWithClassifier.class,
    Nexus688ReindexOnRepoAdd.class,
    Nexus384DotAndDashSearchTest.class,
    Nexus642SynchShadowTaskTest.class,
    Nexus778SearchResultsFilteringTest.class,
    Nexus779RssFeedFilteringTest.class,
    Nexus930AutoDiscoverComponent.class,
    Nexus947GroupBrowsing.class,
    Nexus570IndexArchetypeTest.class,
    Nexus504ChangeRoleTest.class,
    Nexus999SetUsersPassword.class,
    Nexus537RepoTargetsTests.class,
    Nexus1239PlexusUserResourceTest.class,
    Nexus1239UserSearchTest.class,
    Nexus1240SourceInLoginResourceTest.class//,
    //Nexus1592ViewPrivTest.class
} )
public class IntegrationTestSuiteClassesSecurity
{
    @BeforeClass
    public static void beforeSuite()
        throws Exception
    {

        System.out
            .println( "\n\n\n****************************\n   Running Security Tests   \n****************************\n\n\n" );

        // enable security
        TestContainer.getInstance().getTestContext().setSecureTest( true );

    }
}
