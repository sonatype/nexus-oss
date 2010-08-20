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

import org.testng.annotations.BeforeClass;

//@RunWith( Suite.class )
//@SuiteClasses( {
//    Nexus166SampleIT.class,
//    Nexus1170ReducePermissionCheckingIT.class,
//    Nexus758StatusService.class,
//    Nexus169ReleaseMetaDataInSnapshotRepoIT.class,
//    Nexus258ReleaseDeployIT.class,
//    Nexus167ReleaseToSnapshotIT.class,
//    Nexus168SnapshotToReleaseIT.class,
//    Nexus176DeployToInvalidRepoIT.class,
//    Nexus259SnapshotDeployIT.class,
//    Nexus260MultipleDeployIT.class,
//    Nexus261NexusGroupDownloadIT.class,
//    Nexus177OutOfServiceIT.class,
//    Nexus178BlockProxyDownloadIT.class,
//    Nexus179RemoteRepoDownIT.class,
//    Nexus262SimpleProxyIT.class,
//    Nexus133TargetCrudJsonIT.class,
//    Nexus133TargetCrudXmlIT.class,
////    /*
////      Depends on https://issues.sonatype.org/browse/NEXUS-1302
////
////      Nexus142UserCrudJsonTests.class,
////      Nexus142UserCrudXmlTests.class,
////     */
//    Nexus156RolesCrudJsonIT.class,
//    Nexus156RolesCrudXmlIT.class,
//    Nexus142UserValidationIT.class,
//    Nexus156RolesValidationIT.class,
//    Nexus133TargetValidationIT.class,
//    Nexus233PrivilegesCrudXMLIT.class,
//    Nexus233PrivilegesValidationIT.class,
//    Nexus385RoutesCrudXmlIT.class,
//    Nexus385RoutesValidationIT.class,
//    Nexus387RoutesIT.class,
//    Nexus429UploadArtifactPrivilegeIT.class,
//    Nexus133TargetPermissionIT.class,
//    Nexus142UserPermissionIT.class,
//    Nexus156RolesPermissionIT.class,
//    Nexus385RoutesPermissionIT.class,
//    Nexus429WagonDeployPrivilegeIT.class,
//    Nexus393ResetPasswordPermissionIT.class,
//    Nexus394ForgotPasswordPermissionIT.class,
//    Nexus385RoutesPermissionIT.class,
//    Nexus395ForgotUsernamePermissionIT.class,
//    Nexus408ChangePasswordPermissionIT.class,
//    Nexus450UserCreationIT.class,
//    Nexus502MavenExecutionIT.class,
//    Nexus477ArtifactsCrudIT.class,
//    Nexus174ReleaseDeployWrongPasswordIT.class,
//    Nexus175SnapshotDeployWrongPasswordIT.class,
//    Nexus511MavenDeployIT.class,
//    Nexus531RepositoryCrudXMLIT.class,
//    Nexus531RepositoryCrudJsonIT.class,
//    Nexus233PrivilegesCrudXMLIT.class,
//    Nexus379VirtualRepoSameIdIT.class,
//    Nexus448PrivilegeUrlIT.class,
//    Nexus532GroupsCrudXmlIT.class,
//    Nexus586ValidateConfigurationIT.class,
//    Nexus606DownloadLogsAndConfigFilesIT.class,
//    Nexus652Beta5To10UpgradeIT.class,
//    Nexus650ChangePasswordAndRebootIT.class,
//    Nexus725InitialRestClientIT.class,
//    Nexus531RepositoryCrudValidationIT.class,
//    Nexus531RepositoryCrudPermissionIT.class,
//    Nexus532GroupCrudPermissionIT.class,
//    Nexus810PackageNamesInRestMessagesIT.class,
//    Nexus810PackageNamesInNexusConfIT.class,
//    Nexus782UploadWithClassifierIT.class,
//    Nexus688ReindexOnRepoAddIT.class,
//    Nexus384DotAndDashSearchIT.class,
//    Nexus642SynchShadowTaskIT.class,
//    Nexus778SearchResultsFilteringIT.class,
//    Nexus779RssFeedFilteringIT.class,
//    Nexus930AutoDiscoverComponentIT.class,
//    Nexus947GroupBrowsingIT.class,
//    Nexus570IndexArchetypeIT.class,
//    Nexus504ChangeRoleIT.class,
//    Nexus999SetUsersPasswordIT.class,
//    Nexus537RepoTargetsIT.class,
//    Nexus1071DeployToRepoAnonCannotAccessIT.class,
//    Nexus1071AnonAccessIT.class,
//    Nexus1239PlexusUserResourceIT.class,
//    Nexus1239UserSearchIT.class,
//    Nexus1240SourceInLoginResourceIT.class,
//    //Nexus1592ViewPrivTest.class
//    Nexus1560LegacyAllowRulesIT.class,
//    Nexus1560LegacyAllowGroupRulesIT.class,
//    Nexus1560LegacyDenyRulesIT.class,
//    Nexus1563ExternalRealmsLoginIT.class,
//    Nexus383SearchPermissionIT.class
//} )
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
