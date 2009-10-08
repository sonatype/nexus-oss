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
import org.sonatype.nexus.integrationtests.client.nexus725.Nexus725InitialRestClientIT;
import org.sonatype.nexus.integrationtests.client.nexus758.Nexus758StatusService;
import org.sonatype.nexus.integrationtests.nexus1071.Nexus1071AnonAccessIT;
import org.sonatype.nexus.integrationtests.nexus1071.Nexus1071DeployToRepoAnonCannotAccessIT;
import org.sonatype.nexus.integrationtests.nexus1170.Nexus1170ReducePermissionCheckingIT;
import org.sonatype.nexus.integrationtests.nexus1239.Nexus1239PlexusUserResourceIT;
import org.sonatype.nexus.integrationtests.nexus1239.Nexus1239UserSearchIT;
import org.sonatype.nexus.integrationtests.nexus1240.Nexus1240SourceInLoginResourceIT;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetCrudJsonIT;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetCrudXmlIT;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetPermissionIT;
import org.sonatype.nexus.integrationtests.nexus133.Nexus133TargetValidationIT;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserPermissionIT;
import org.sonatype.nexus.integrationtests.nexus142.Nexus142UserValidationIT;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesCrudJsonIT;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesCrudXmlIT;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesPermissionIT;
import org.sonatype.nexus.integrationtests.nexus156.Nexus156RolesValidationIT;
import org.sonatype.nexus.integrationtests.nexus1560.Nexus1560LegacyAllowGroupRulesIT;
import org.sonatype.nexus.integrationtests.nexus1560.Nexus1560LegacyAllowRulesIT;
import org.sonatype.nexus.integrationtests.nexus1560.Nexus1560LegacyDenyRulesIT;
import org.sonatype.nexus.integrationtests.nexus1563.Nexus1563ExternalRealmsLoginIT;
import org.sonatype.nexus.integrationtests.nexus166.Nexus166SampleIT;
import org.sonatype.nexus.integrationtests.nexus167.Nexus167ReleaseToSnapshotIT;
import org.sonatype.nexus.integrationtests.nexus168.Nexus168SnapshotToReleaseIT;
import org.sonatype.nexus.integrationtests.nexus169.Nexus169ReleaseMetaDataInSnapshotRepoIT;
import org.sonatype.nexus.integrationtests.nexus174.Nexus174ReleaseDeployWrongPasswordIT;
import org.sonatype.nexus.integrationtests.nexus175.Nexus175SnapshotDeployWrongPasswordIT;
import org.sonatype.nexus.integrationtests.nexus176.Nexus176DeployToInvalidRepoIT;
import org.sonatype.nexus.integrationtests.nexus233.Nexus233PrivilegesCrudXMLIT;
import org.sonatype.nexus.integrationtests.nexus233.Nexus233PrivilegesValidationIT;
import org.sonatype.nexus.integrationtests.nexus258.Nexus258ReleaseDeployIT;
import org.sonatype.nexus.integrationtests.nexus259.Nexus259SnapshotDeployIT;
import org.sonatype.nexus.integrationtests.nexus260.Nexus260MultipleDeployIT;
import org.sonatype.nexus.integrationtests.nexus261.Nexus261NexusGroupDownloadIT;
import org.sonatype.nexus.integrationtests.nexus379.Nexus379VirtualRepoSameIdIT;
import org.sonatype.nexus.integrationtests.nexus383.Nexus383SearchPermissionIT;
import org.sonatype.nexus.integrationtests.nexus384.Nexus384DotAndDashSearchIT;
import org.sonatype.nexus.integrationtests.nexus385.Nexus385RoutesCrudXmlIT;
import org.sonatype.nexus.integrationtests.nexus385.Nexus385RoutesPermissionIT;
import org.sonatype.nexus.integrationtests.nexus385.Nexus385RoutesValidationIT;
import org.sonatype.nexus.integrationtests.nexus387.Nexus387RoutesIT;
import org.sonatype.nexus.integrationtests.nexus393.Nexus393ResetPasswordPermissionIT;
import org.sonatype.nexus.integrationtests.nexus394.Nexus394ForgotPasswordPermissionIT;
import org.sonatype.nexus.integrationtests.nexus395.Nexus395ForgotUsernamePermissionIT;
import org.sonatype.nexus.integrationtests.nexus408.Nexus408ChangePasswordPermissionIT;
import org.sonatype.nexus.integrationtests.nexus429.Nexus429UploadArtifactPrivilegeIT;
import org.sonatype.nexus.integrationtests.nexus429.Nexus429WagonDeployPrivilegeIT;
import org.sonatype.nexus.integrationtests.nexus448.Nexus448PrivilegeUrlIT;
import org.sonatype.nexus.integrationtests.nexus450.Nexus450UserCreationIT;
import org.sonatype.nexus.integrationtests.nexus477.Nexus477ArtifactsCrudIT;
import org.sonatype.nexus.integrationtests.nexus502.Nexus502MavenExecutionIT;
import org.sonatype.nexus.integrationtests.nexus504.Nexus504ChangeRoleIT;
import org.sonatype.nexus.integrationtests.nexus511.Nexus511MavenDeployIT;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudJsonIT;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudPermissionIT;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudValidationIT;
import org.sonatype.nexus.integrationtests.nexus531.Nexus531RepositoryCrudXMLIT;
import org.sonatype.nexus.integrationtests.nexus532.Nexus532GroupCrudPermissionIT;
import org.sonatype.nexus.integrationtests.nexus532.Nexus532GroupsCrudXmlIT;
import org.sonatype.nexus.integrationtests.nexus537.Nexus537RepoTargetsIT;
import org.sonatype.nexus.integrationtests.nexus570.Nexus570IndexArchetypeIT;
import org.sonatype.nexus.integrationtests.nexus586.Nexus586ValidateConfigurationIT;
import org.sonatype.nexus.integrationtests.nexus606.Nexus606DownloadLogsAndConfigFilesIT;
import org.sonatype.nexus.integrationtests.nexus642.Nexus642SynchShadowTaskIT;
import org.sonatype.nexus.integrationtests.nexus650.Nexus650ChangePasswordAndRebootIT;
import org.sonatype.nexus.integrationtests.nexus688.Nexus688ReindexOnRepoAddIT;
import org.sonatype.nexus.integrationtests.nexus778.Nexus778SearchResultsFilteringIT;
import org.sonatype.nexus.integrationtests.nexus779.Nexus779RssFeedFilteringIT;
import org.sonatype.nexus.integrationtests.nexus782.Nexus782UploadWithClassifierIT;
import org.sonatype.nexus.integrationtests.nexus810.Nexus810PackageNamesInNexusConfIT;
import org.sonatype.nexus.integrationtests.nexus810.Nexus810PackageNamesInRestMessagesIT;
import org.sonatype.nexus.integrationtests.nexus930.Nexus930AutoDiscoverComponentIT;
import org.sonatype.nexus.integrationtests.nexus947.Nexus947GroupBrowsingIT;
import org.sonatype.nexus.integrationtests.nexus999.Nexus999SetUsersPasswordIT;
import org.sonatype.nexus.integrationtests.proxy.nexus177.Nexus177OutOfServiceIT;
import org.sonatype.nexus.integrationtests.proxy.nexus178.Nexus178BlockProxyDownloadIT;
import org.sonatype.nexus.integrationtests.proxy.nexus179.Nexus179RemoteRepoDownIT;
import org.sonatype.nexus.integrationtests.proxy.nexus262.Nexus262SimpleProxyIT;
import org.sonatype.nexus.integrationtests.upgrades.nexus652.Nexus652Beta5To10UpgradeIT;

@RunWith( Suite.class )
@SuiteClasses( {
    Nexus166SampleIT.class,
    Nexus1170ReducePermissionCheckingIT.class,
    Nexus758StatusService.class,
    Nexus169ReleaseMetaDataInSnapshotRepoIT.class,
    Nexus258ReleaseDeployIT.class,
    Nexus167ReleaseToSnapshotIT.class,
    Nexus168SnapshotToReleaseIT.class,
    Nexus176DeployToInvalidRepoIT.class,
    Nexus259SnapshotDeployIT.class,
    Nexus260MultipleDeployIT.class,
    Nexus261NexusGroupDownloadIT.class,
    Nexus177OutOfServiceIT.class,
    Nexus178BlockProxyDownloadIT.class,
    Nexus179RemoteRepoDownIT.class,
    Nexus262SimpleProxyIT.class,
    Nexus133TargetCrudJsonIT.class,
    Nexus133TargetCrudXmlIT.class,
    /*
      Depends on https://issues.sonatype.org/browse/NEXUS-1302

      Nexus142UserCrudJsonTests.class,
      Nexus142UserCrudXmlTests.class,
     */
    Nexus156RolesCrudJsonIT.class,
    Nexus156RolesCrudXmlIT.class,
    Nexus142UserValidationIT.class,
    Nexus156RolesValidationIT.class,
    Nexus133TargetValidationIT.class,
    Nexus233PrivilegesCrudXMLIT.class,
    Nexus233PrivilegesValidationIT.class,
    Nexus385RoutesCrudXmlIT.class,
    Nexus385RoutesValidationIT.class,
    Nexus387RoutesIT.class,
    Nexus429UploadArtifactPrivilegeIT.class,
    Nexus133TargetPermissionIT.class,
    Nexus142UserPermissionIT.class,
    Nexus156RolesPermissionIT.class,
    Nexus385RoutesPermissionIT.class,
    Nexus429WagonDeployPrivilegeIT.class,
    Nexus393ResetPasswordPermissionIT.class,
    Nexus394ForgotPasswordPermissionIT.class,
    Nexus385RoutesPermissionIT.class,
    Nexus395ForgotUsernamePermissionIT.class,
    Nexus408ChangePasswordPermissionIT.class,
    Nexus450UserCreationIT.class,
    Nexus502MavenExecutionIT.class,
    Nexus477ArtifactsCrudIT.class,
    Nexus174ReleaseDeployWrongPasswordIT.class,
    Nexus175SnapshotDeployWrongPasswordIT.class,
    Nexus511MavenDeployIT.class,
    Nexus531RepositoryCrudXMLIT.class,
    Nexus531RepositoryCrudJsonIT.class,
    Nexus233PrivilegesCrudXMLIT.class,
    Nexus379VirtualRepoSameIdIT.class,
    Nexus448PrivilegeUrlIT.class,
    Nexus532GroupsCrudXmlIT.class,
    Nexus586ValidateConfigurationIT.class,
    Nexus606DownloadLogsAndConfigFilesIT.class,
    Nexus652Beta5To10UpgradeIT.class,
    Nexus650ChangePasswordAndRebootIT.class,
    Nexus725InitialRestClientIT.class,
    Nexus531RepositoryCrudValidationIT.class,
    Nexus531RepositoryCrudPermissionIT.class,
    Nexus532GroupCrudPermissionIT.class,
    Nexus810PackageNamesInRestMessagesIT.class,
    Nexus810PackageNamesInNexusConfIT.class,
    Nexus782UploadWithClassifierIT.class,
    Nexus688ReindexOnRepoAddIT.class,
    Nexus384DotAndDashSearchIT.class,
    Nexus642SynchShadowTaskIT.class,
    Nexus778SearchResultsFilteringIT.class,
    Nexus779RssFeedFilteringIT.class,
    Nexus930AutoDiscoverComponentIT.class,
    Nexus947GroupBrowsingIT.class,
    Nexus570IndexArchetypeIT.class,
    Nexus504ChangeRoleIT.class,
    Nexus999SetUsersPasswordIT.class,
    Nexus537RepoTargetsIT.class,
    Nexus1071DeployToRepoAnonCannotAccessIT.class,
    Nexus1071AnonAccessIT.class,
    Nexus1239PlexusUserResourceIT.class,
    Nexus1239UserSearchIT.class,
    Nexus1240SourceInLoginResourceIT.class,
    //Nexus1592ViewPrivTest.class
    Nexus1560LegacyAllowRulesIT.class,
    Nexus1560LegacyAllowGroupRulesIT.class,
    Nexus1560LegacyDenyRulesIT.class,
    Nexus1563ExternalRealmsLoginIT.class,
    Nexus383SearchPermissionIT.class
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
