/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Default nexus static security resource.
 *
 * @since 3.0
 */
@Named
@Singleton
class SecurityResource
implements StaticSecurityResource
{
  @Override
  Configuration getConfiguration() {
    return new Configuration(
        privileges: [
            new CPrivilege(
                id: 'metrics-endpoints',
                type: 'method',
                name: 'Metrics Endpoints',
                description: 'Allows access to metrics endpoints.',
                properties: [
                    'method': '*',
                    'permission': 'nexus:metrics-endpoints'
                ]
            ),
            new CPrivilege(
                id: 'repository-m2-read', // T1
                type: 'target',
                name: 'All M2 Repositories - (read)',
                description: 'Give permission to read any content in any Maven 2 type repositories.',
                properties: [
                    'method': 'read',
                    'repositoryTargetId': '1',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m1-read', // T2
                type: 'target',
                name: 'All M1 Repositories - (read)',
                description: 'Give permission to read any content in any Maven 1 type repositories.',
                properties: [
                    'method': 'read',
                    'repositoryTargetId': '2',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m2-update', // T3
                type: 'target',
                name: 'All M2 Repositories - (update)',
                description: 'Give permission to update any content in any Maven 2 type repositories.',
                properties: [
                    'method': 'update,read',
                    'repositoryTargetId': '1',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m1-update', // T4
                type: 'target',
                name: 'All M1 Repositories - (update)',
                description: 'Give permission to update any content in any Maven 1 type repositories.',
                properties: [
                    'method': 'update,read',
                    'repositoryTargetId': '2',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m2-create', // T5
                type: 'target',
                name: 'All M2 Repositories - (create)',
                description: 'Give permission to create any content in any Maven 2 type repositories.',
                properties: [
                    'method': 'create,read',
                    'repositoryTargetId': '1',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m1-create', // T6
                type: 'target',
                name: 'All M1 Repositories - (create)',
                description: 'Give permission to create any content in any Maven 1 type repositories.',
                properties: [
                    'method': 'create,read',
                    'repositoryTargetId': '2',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m2-delete', // T7
                type: 'target',
                name: 'All M2 Repositories - (delete)',
                description: 'Give permission to delete any content in any Maven 2 type repositories.',
                properties: [
                    'method': 'delete,read',
                    'repositoryTargetId': '1',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m1-delete', // T8
                type: 'target',
                name: 'All M1 Repositories - (delete)',
                description: 'Give permission to delete any content in any Maven 1 type repositories.',
                properties: [
                    'method': 'delete,read',
                    'repositoryTargetId': '2',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'repository-any-read', // T9
                type: 'target',
                name: 'All Repositories - (read)',
                description: 'Give permission to read any content in any repository.',
                properties: [
                    'method': 'read',
                    'repositoryTargetId': 'any',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'repository-any-update', // T10
                type: 'target',
                name: 'All Repositories - (update)',
                description: 'Give permission to update any content in any repository.',
                properties: [
                    'method': 'update,read',
                    'repositoryTargetId': 'any',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'repository-any-create', // T11
                type: 'target',
                name: 'All Repositories - (create)',
                description: 'Give permission to create any content in any repository.',
                properties: [
                    'method': 'create,read',
                    'repositoryTargetId': 'any',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'repository-any-delete', // T12
                type: 'target',
                name: 'All Repositories - (delete)',
                description: 'Give permission to delete any content in any repository.',
                properties: [
                    'method': 'delete,read',
                    'repositoryTargetId': 'any',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'admin', // 1000
                type: 'method',
                name: 'Administrator privilege (ALL)',
                description: 'Give permission to everything available in nexus.',
                properties: [
                    'method': '*',
                    'permission': 'nexus:*'
                ]
            ),
            new CPrivilege(
                id: 'status', // 1
                type: 'method',
                name: 'Status - (read)',
                description: 'Give permission to query the nexus server for it\'s status.  This privilege is required by the anonymous user so that the UI can retrieve anonymous permissions on startup.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:status'
                ]
            ),
            new CPrivilege(
                id: 'signin', // 2
                type: 'method',
                name: 'Sign-in',
                description: 'Give permission to allow a user to sign-in into nexus.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:authentication'
                ]
            ),
            new CPrivilege(
                id: 'settings-read', // 3
                type: 'method',
                name: 'Server Settings - (read)',
                description: 'Give permission to read the nexus server settings.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:settings'
                ]
            ),
            new CPrivilege(
                id: 'settings-update', // 4
                type: 'method',
                name: 'Server Settings - (update,read)',
                description: 'Give permission to update,read the nexus server settings.',
                properties: [
                    'method': 'update,read',
                    'permission': 'nexus:settings'
                ]
            ),
            new CPrivilege(
                id: 'repositories-create', // 5
                type: 'method',
                name: 'Repositories - (create,read)',
                description: 'Give permission to create,read new repositories.',
                properties: [
                    'method': 'create,read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repositories-read', // 6
                type: 'method',
                name: 'Repositories - (read)',
                description: 'Give permission to read existing repository configuration.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repositories-update', // 7
                type: 'method',
                name: 'Repositories - (update,read)',
                description: 'Give permission to update,read existing repository configuration.',
                properties: [
                    'method': 'update,read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repositories-delete', // 8
                type: 'method',
                name: 'Repositories - (delete,read)',
                description: 'Give permission to delete,read existing repositories.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repotemplates-create', // 9
                type: 'method',
                name: 'Repository Templates - (create,read)',
                description: 'Give permission to create,read new repository templates.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    'method': 'create,read',
                    'permission': 'nexus:repotemplates'
                ]
            ),
            new CPrivilege(
                id: 'repotemplates-read', // 10
                type: 'method',
                name: 'Repository Templates - (read)',
                description: 'Give permission to read existing repository template configuration.  This privilege is required to add a new Repository in the UI, as the default values are retrieved from the template on the server.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:repotemplates'
                ]
            ),
            new CPrivilege(
                id: 'repotemplates-update',  // 11
                type: 'method',
                name: 'Repository Templates - (update,read)',
                description: 'Give permission to update,read existing repository template configuration.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    'method': 'update,read',
                    'permission': 'nexus:repotemplates'
                ]
            ),
            new CPrivilege(
                id: 'repotemplates-delete', // 12
                type: 'method',
                name: 'Repository Templates - (delete,read)',
                description: 'Give permission to delete,read existing repository templates.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'nexus:repotemplates'
                ]
            ),
            new CPrivilege(
                id: 'repogroups-create', // 13
                type: 'method',
                name: 'Repository Groups - (create,read)',
                description: 'Give permission to create,read new repository groups.',
                properties: [
                    'method': 'create,read',
                    'permission': 'nexus:repogroups'
                ]
            ),
            new CPrivilege(
                id: 'repogroups-read', // 14
                type: 'method',
                name: 'Repository Groups - (read)',
                description: 'Give permission to read existing repository group configuration.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:repogroups'
                ]
            ),
            new CPrivilege(
                id: 'repogroups-update', // 15
                type: 'method',
                name: 'Repository Groups - (update,read)',
                description: 'Give permission to update,read existing repository group configuration.',
                properties: [
                    'method': 'update,read',
                    'permission': 'nexus:repogroups'
                ]
            ),
            new CPrivilege(
                id: 'repogroups-delete', // 16
                type: 'method',
                name: 'Repository Groups - (delete,read)',
                description: 'Give permission to delete,read existing repository groups.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'nexus:repogroups'
                ]
            ),
            new CPrivilege(
                id: 'attributes-delete', // 20
                type: 'method',
                name: 'Rebuild Repository Attributes',
                description: 'Give permission to rebuild the attributes of repository content.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'nexus:attributes'
                ]
            ),
            new CPrivilege(
                id: 'cache-delete', // 21
                type: 'method',
                name: 'Clear Repository Cache',
                description: 'Give permission to clear the content of a repositories not found cache.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'nexus:cache'
                ]
            ),
            new CPrivilege(
                id: 'routes-create', // 22
                type: 'method',
                name: 'Repository Routes - (create,read)',
                description: 'Give permission to create,read repository routes.',
                properties: [
                    'method': 'create,read',
                    'permission': 'nexus:routes'
                ]
            ),
            new CPrivilege(
                id: 'routes-read', // 23
                type: 'method',
                name: 'Repository Routes - (read)',
                description: 'Give permission to read existing repository route configuration.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:routes'
                ]
            ),
            new CPrivilege(
                id: 'routes-update', // 24
                type: 'method',
                name: 'Repository Routes - (update,read)',
                description: 'Give permission to update,read existing repository route configuration.',
                properties: [
                    'method': 'update,read',
                    'permission': 'nexus:routes'
                ]
            ),
            new CPrivilege(
                id: 'routes-delete', // 25
                type: 'method',
                name: 'Repository Routes - (delete,read)',
                description: 'Give permission to delete,read existing repository routes.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'nexus:routes'
                ]
            ),
            new CPrivilege(
                id: 'tasks-create', // 26
                type: 'method',
                name: 'Scheduled Tasks - (create,read)',
                description: 'Give permission to create,read scheduled tasks.',
                properties: [
                    'method': 'create,read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasks-read', // 27
                type: 'method',
                name: 'Scheduled Tasks - (read)',
                description: 'Give permission to read existing scheduled task configuration.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasks-update', // 28
                type: 'method',
                name: 'Scheduled Tasks - (update,read)',
                description: 'Give permission to update,read existing scheduled task configuration.',
                properties: [
                    'method': 'update,read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasks-delete', // 29
                type: 'method',
                name: 'Scheduled Tasks - (delete,read)',
                description: 'Give permission to delete,read existing scheduled tasks.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'logs-read', // 42
                type: 'method',
                name: 'Logs - (read)',
                description: 'Give permission to retrieve the nexus log files.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:logs'
                ]
            ),
            new CPrivilege(
                id: 'configuration-read', // 43
                type: 'method',
                name: 'Configuration File - (read)',
                description: 'Give permission to retrieve the nexus.xml configuration file.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:configuration'
                ]
            ),
            new CPrivilege(
                id: 'targets-create', // 45
                type: 'method',
                name: 'Repository Targets - (create,read)',
                description: 'Give permission to create,read repository targets.',
                properties: [
                    'method': 'create,read',
                    'permission': 'nexus:targets'
                ]
            ),
            new CPrivilege(
                id: 'targets-read', // 46
                type: 'method',
                name: 'Repository Targets - (read)',
                description: 'Give permission to read existing repository target configuration.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:targets'
                ]
            ),
            new CPrivilege(
                id: 'targets-update', // 47
                type: 'method',
                name: 'Repository Targets - (update,read)',
                description: 'Give permission to update,read existing repository target configuration.',
                properties: [
                    'method': 'update,read',
                    'permission': 'nexus:targets'
                ]
            ),
            new CPrivilege(
                id: 'targets-delete', // 48
                type: 'method',
                name: 'Repository Targets - (delete,read)',
                description: 'Give permission to delete,read existing repository targets.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'nexus:targets'
                ]
            ),
            new CPrivilege(
                id: 'wastebasket-read', // 50
                type: 'method',
                name: 'Wastebasket - (read)',
                description: 'Give permission to read the contents of the nexus trash.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:wastebasket'
                ]
            ),
            new CPrivilege(
                id: 'wastebasket-delete', // 51
                type: 'method',
                name: 'Wastebasket - (delete,read)',
                description: 'Give permission to delete,read the contents of the nexus trash.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'nexus:wastebasket'
                ]
            ),
            new CPrivilege(
                id: 'artifact-read', // 54
                type: 'method',
                name: 'Artifact Download',
                description: 'Give permission to download artifacts (using /artifact service, not the /content url).  The extents of this privilege are related to the allowed targets..  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:artifact'
                ]
            ),
            new CPrivilege(
                id: 'repostatus-read', // 55
                type: 'method',
                name: 'Read Repository Status',
                description: 'Give permission to retrieve the status of a repository.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:repostatus'
                ]
            ),
            new CPrivilege(
                id: 'repostatus-update', // 56
                type: 'method',
                name: 'Update Repository Status',
                description: 'Give permission to update the status of a repository.',
                properties: [
                    'method': 'update',
                    'permission': 'nexus:repostatus'
                ]
            ),
            new CPrivilege(
                id: 'artifact-create', // 65
                type: 'method',
                name: 'Artifact Upload',
                description: 'Give permission to upload artifacts (using /artifact service, not the /content service).  The extents of this privilege are related to the allowed targets.',
                properties: [
                    'method': 'create,read',
                    'permission': 'nexus:artifact'
                ]
            ),
            new CPrivilege(
                id: 'repometa-read', // 67
                type: 'method',
                name: 'Repository Summary Info (read)',
                description: 'Give permission to read the repository summary information.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:repometa'
                ]
            ),
            new CPrivilege(
                id: 'tasksrun', // 68
                type: 'method',
                name: 'Scheduled Tasks - (run,stop)',
                description: 'Give permission to run and stop existing scheduled tasks manually.',
                properties: [
                    'method': 'read,delete',
                    'permission': 'nexus:tasksrun'
                ]
            ),
            new CPrivilege(
                id: 'tasktypes-read', // 69
                type: 'method',
                name: 'Scheduled Task Types - (read)',
                description: 'Give permission to retrieve list of support task types available in nexus.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:tasktypes'
                ]
            ),
            new CPrivilege(
                id: 'contentclasses-read', // 70
                type: 'method',
                name: 'Repository Content Classes Component - (read)',
                description: 'Give permission to retrieve the list of repository content classes supported by nexus.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:componentscontentclasses'
                ]
            ),
            new CPrivilege(
                id: 'scheduletypes-read', // 71
                type: 'method',
                name: 'Scheduled Task Types Component - (read)',
                description: 'Give permission to retrieve list of support task types available in nexus.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:componentscheduletypes'
                ]
            ),
            new CPrivilege(
                id: 'realmtypes-read', // 73
                type: 'method',
                name: 'Realm Types Component - (read)',
                description: 'Give permission to retrieve list of support realms available in nexus.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:componentrealmtypes'
                ]
            ),
            new CPrivilege(
                id: '74',
                type: 'method',
                name: 'Repository Types - (read)',
                description: 'Give permission to retrieve the list of repository types supported by nexus.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:componentsrepotypes'
                ]
            ),
            new CPrivilege(
                id: '76',
                type: 'method',
                name: 'Rebuild Maven Metadata',
                description: 'Give permission to rebuild the maven metadata of repository content.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'nexus:metadata'
                ]
            ),
            new CPrivilege(
                id: '77',
                type: 'method',
                name: 'Log Configuration - (read,update)',
                description: 'Give permission to read and update log configuration',
                properties: [
                    'method': 'read,update',
                    'permission': 'nexus:logconfig'
                ]
            ),
            new CPrivilege(
                id: '78',
                type: 'method',
                name: 'Repository Mirrors - (read)',
                description: 'Give permission to read repository mirror configuration',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:repositorymirrors'
                ]
            ),
            new CPrivilege(
                id: '79',
                type: 'method',
                name: 'Repository Mirrors - (create,read)',
                description: 'Give permission to create repository mirror configuration',
                properties: [
                    'method': 'create,read',
                    'permission': 'nexus:repositorymirrors'
                ]
            ),
            new CPrivilege(
                id: '81',
                type: 'method',
                name: 'Repository Mirrors, Retrieve Predefined List - (read)',
                description: 'Give permission to retrieve predefined list of mirrors from the server',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:repositorypredefinedmirrors'
                ]
            ),
            new CPrivilege(
                id: '82',
                type: 'method',
                name: 'Repository Mirrors, Retrieve Mirror Status - (read)',
                description: 'Give permission to retrieve mirror status from the server',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:repositorymirrorsstatus'
                ]
            ),
            new CPrivilege(
                id: '83',
                type: 'method',
                name: 'API-Key Access',
                description: 'Give permission to use an API-Key to access the server.',
                properties: [
                    'method': '*',
                    'permission': 'apikey:access'
                ]
            )
        ],
        roles: [
            new CRole(
                id: 'nx-admin',
                name: 'Nexus Administrator Role',
                description: 'Administration role for Nexus',
                privileges: ['admin', '1001', '83']
            ),
            new CRole(
                id: 'nx-deployment',
                name: 'Nexus Deployment Role',
                description: 'Deployment role for Nexus',
                privileges: ['83'],
                roles: ['anonymous', 'ui-basic']
            ),
            new CRole(
                id: 'anonymous',
                name: 'Nexus Anonymous Role',
                description: 'Anonymous role for Nexus',
                privileges: ['status', 'artifact-read', '57', '58', 'contentclasses-read', '74'],
                roles: ['ui-repo-browser']
            ),
            new CRole(
                id: 'nx-developer',
                name: 'Nexus Developer Role',
                description: 'Developer role for Nexus',
                roles: ['ui-basic', 'nx-deployment']
            ),
            new CRole(
                id: 'repo-all-read',
                name: 'Repo: All Maven Repositories (Read)',
                description: 'Gives access to read ALL content of ALL Maven1 and Maven2 repositories in Nexus.',
                privileges: ['repository-m2-read', 'repository-m1-read', 'repository-all']
            ),
            new CRole(
                id: 'repo-all-full',
                name: 'Repo: All Maven Repositories (Full Control)',
                description: 'Gives access to create/read/update/delete ALL content of ALL Maven1 and Maven2 repositories in Nexus.',
                privileges: ['repository-m2-read', 'repository-m1-read', 'repository-m2-update', 'repository-m1-update', 'repository-m2-create', 'repository-m1-create', 'repository-m2-delete', 'repository-m1-delete', 'repository-all']
            ),
            new CRole(
                id: 'repository-any-read',
                name: 'Repo: All Repositories (Read)',
                description: 'Gives access to read ALL content of ALL repositories in Nexus.',
                privileges: ['repository-any-read', 'repository-all']
            ),
            new CRole(
                id: 'repository-any-full',
                name: 'Repo: All Repositories (Full Control)',
                description: 'Gives access to create/read/update/delete ALL content of ALL repositories in Nexus.',
                privileges: ['repository-any-read', 'repository-any-update', 'repository-any-create', 'repository-any-delete', 'repository-all']
            ),
            new CRole(
                id: 'ui-repo-browser',
                name: 'UI: Repository Browser',
                description: 'Gives access to the Repository Browser screen in Nexus UI',
                privileges: ['repositories-read', 'repogroups-read', 'repostatus-read']
            ),
            new CRole(
                id: 'ui-logs-config-files',
                name: 'UI: Logs and Config Files',
                description: 'Gives access to the Logs and Config Files screen in Nexus UI',
                privileges: ['logs-read', 'configuration-read']
            ),
            new CRole(
                id: 'ui-server-admin',
                name: 'UI: Server Administration',
                description: 'Gives access to the Server Administration screen in Nexus UI',
                privileges: ['settings-read', 'settings-update', 'realmtypes-read']
            ),
            new CRole(
                id: 'ui-repository-admin',
                name: 'UI: Repository Administration',
                description: 'Gives access to the Repository Administration screen in Nexus UI',
                privileges: ['repositories-create', 'repositories-read', 'repositories-update', 'repositories-delete', 'repotemplates-read', '78', '79', '81', '82', '74', 'repository-all'],
                roles: ['ui-repo-browser']
            ),
            new CRole(
                id: 'ui-group-admin',
                name: 'UI: Group Administration',
                description: 'Gives access to the Group Administration screen in Nexus UI',
                privileges: ['repositories-read', 'repogroups-create', 'repogroups-read', 'repogroups-update', 'repogroups-delete', 'repository-all'],
                roles: ['ui-repo-browser']
            ),
            new CRole(
                id: 'ui-routing-admin',
                name: 'UI: Routing Administration',
                description: 'Gives access to the Routing Administration screen in Nexus UI',
                privileges: ['repositories-read', 'repogroups-read', 'routes-create', 'routes-read', 'routes-update', 'routes-delete']
            ),
            new CRole(
                id: 'ui-scheduled-tasks-admin',
                name: 'UI: Scheduled Task Administration',
                description: 'Gives access to the Scheduled Task Administration screen in Nexus UI',
                privileges: ['repositories-read', 'repogroups-read', 'tasks-create', 'tasks-read', 'tasks-update', 'tasks-delete', 'tasksrun', 'tasktypes-read', 'scheduletypes-read']
            ),
            new CRole(
                id: 'ui-repository-targets-admin',
                name: 'UI: Repository Target Administration',
                description: 'Gives access to the Repository Target Administration screen in Nexus UI',
                privileges: ['targets-create', 'targets-read', 'targets-update', 'targets-delete', 'contentclasses-read', '74']
            ),
            new CRole(
                id: 'ui-users-admin',
                name: 'UI: User Administration',
                description: 'Gives access to the User Administration screen in Nexus UI',
                privileges: ['35', '38', '39', '40', '41', '72', '75']
            ),
            new CRole(
                id: 'ui-roles-admin',
                name: 'UI: Role Administration',
                description: 'Gives access to the Role Administration screen in Nexus UI',
                privileges: ['31', '34', '35', '36', '37']
            ),
            new CRole(
                id: 'ui-privileges-admin',
                name: 'UI: Privilege Administration',
                description: 'Gives access to the Privilege Administration screen in Nexus UI',
                privileges: ['repositories-read', 'repogroups-read', '30', '31', '32', '33', 'targets-read', '80']
            ),
            new CRole(
                id: 'ui-basic',
                name: 'UI: Base UI Privileges',
                description: 'Generic privileges for users in the Nexus UI',
                privileges: ['status', 'signin', '64']
            ),
            new CRole(
                id: 'nx-apikey-access',
                name: 'Nexus API-Key Access',
                description: 'API-Key Access role for Nexus.',
                privileges: ['83']
            ),
            new CRole(
                id: 'metrics-endpoints',
                name: 'Metrics Endpoints',
                description: 'Allows access to metrics endpoints.',
                privileges: ['metrics-endpoints']
            )
        ]
    )
  }
}

