/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.testharness;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sonatype.nexus.integrationtests.AbstractNexusTestSuite;
import org.sonatype.nexus.security.ldap.realms.testharness.nxcm147.Nxcm147SavedPasswordConnectionJsonIT;
import org.sonatype.nexus.security.ldap.realms.testharness.nxcm335.Nxcm335EffectiveUsersIT;
import org.sonatype.nexus.security.ldap.realms.testharness.nxcm355.Nxcm355UserSubTreeNotSavedIT;
import org.sonatype.nexus.security.ldap.realms.testharness.nxcm58.Nxcm58NexusCommonUseJsonIT;


@RunWith( Suite.class )
@SuiteClasses( {
    Nxcm58NexusCommonUseJsonIT.class,
    Nxcm147SavedPasswordConnectionJsonIT.class,
    Nxcm355UserSubTreeNotSavedIT.class,
    Nxcm335EffectiveUsersIT.class/*,
    Nexus758StatusService.class,
    Nexus169ReleaseMetaDataInSnapshotRepoTest.class,
    Nexus258ReleaseDeployTest.class,
    Nexus167ReleaseToSnapshotTest.class,
    Nexus168SnapshotToReleaseTest.class,
    Nexus176DeployToInvalidRepoTest.class,
    Nexus259SnapshotDeployTest.class,
    Nexus260MultipleDeployTest.class,
//    /*

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
//    Nexus156RolesCrudJsonTests.class,
//    Nexus156RolesCrudXmlTests.class,
    Nexus142UserValidationTests.class,
    Nexus156RolesValidationTests.class,
    Nexus133TargetValidationTests.class,
//    Nexus233PrivilegesCrudXMLTests.class,
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
//    Nexus233PrivilegesCrudXMLTests.class,
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
    Nexus537RepoTargetsTests.class //*/

} )
public class LdapTestSuite extends AbstractNexusTestSuite
{

}
