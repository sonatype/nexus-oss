/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.internal.security

import org.sonatype.nexus.security.config.CPrivilege
import org.sonatype.nexus.security.config.CRole
import org.sonatype.nexus.security.config.MemorySecurityConfiguration
import org.sonatype.nexus.security.config.StaticSecurityConfigurationResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Default nexus static security resource.
 *
 * @since 3.0
 */
@Named
@Singleton
class StaticSecurityConfigurationResourceImpl
    implements StaticSecurityConfigurationResource
{
  @Override
  MemorySecurityConfiguration getConfiguration() {
    return new MemorySecurityConfiguration(
        privileges: [
            new CPrivilege(
                id: 'metrics-endpoints',
                type: 'method',
                name: 'Metrics Endpoints',
                description: 'Allows access to metrics endpoints.',
                properties: [
                    'method'    : '*',
                    'permission': 'nexus:metrics-endpoints'
                ]
            ),
            new CPrivilege(
                id: 'repository-m2-read', // T1
                type: 'target',
                name: 'All M2 Repositories - (read)',
                description: 'Give permission to read any content in any Maven 2 type repositories.',
                properties: [
                    'method'            : 'read',
                    'repositoryTargetId': '1',
                    'repositoryId'      : '',
                    'repositoryGroupId' : ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m1-read', // T2
                type: 'target',
                name: 'All M1 Repositories - (read)',
                description: 'Give permission to read any content in any Maven 1 type repositories.',
                properties: [
                    'method'            : 'read',
                    'repositoryTargetId': '2',
                    'repositoryId'      : '',
                    'repositoryGroupId' : ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m2-update', // T3
                type: 'target',
                name: 'All M2 Repositories - (update)',
                description: 'Give permission to update any content in any Maven 2 type repositories.',
                properties: [
                    'method'            : 'update,read',
                    'repositoryTargetId': '1',
                    'repositoryId'      : '',
                    'repositoryGroupId' : ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m1-update', // T4
                type: 'target',
                name: 'All M1 Repositories - (update)',
                description: 'Give permission to update any content in any Maven 1 type repositories.',
                properties: [
                    'method'            : 'update,read',
                    'repositoryTargetId': '2',
                    'repositoryId'      : '',
                    'repositoryGroupId' : ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m2-create', // T5
                type: 'target',
                name: 'All M2 Repositories - (create)',
                description: 'Give permission to create any content in any Maven 2 type repositories.',
                properties: [
                    'method'            : 'create,read',
                    'repositoryTargetId': '1',
                    'repositoryId'      : '',
                    'repositoryGroupId' : ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m1-create', // T6
                type: 'target',
                name: 'All M1 Repositories - (create)',
                description: 'Give permission to create any content in any Maven 1 type repositories.',
                properties: [
                    'method'            : 'create,read',
                    'repositoryTargetId': '2',
                    'repositoryId'      : '',
                    'repositoryGroupId' : ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m2-delete', // T7
                type: 'target',
                name: 'All M2 Repositories - (delete)',
                description: 'Give permission to delete any content in any Maven 2 type repositories.',
                properties: [
                    'method'            : 'delete,read',
                    'repositoryTargetId': '1',
                    'repositoryId'      : '',
                    'repositoryGroupId' : ''
                ]
            ),
            new CPrivilege(
                id: 'repository-m1-delete', // T8
                type: 'target',
                name: 'All M1 Repositories - (delete)',
                description: 'Give permission to delete any content in any Maven 1 type repositories.',
                properties: [
                    'method'            : 'delete,read',
                    'repositoryTargetId': '2',
                    'repositoryId'      : '',
                    'repositoryGroupId' : ''
                ]
            ),
            new CPrivilege(
                id: 'repository-any-read', // T9
                type: 'target',
                name: 'All Repositories - (read)',
                description: 'Give permission to read any content in any repository.',
                properties: [
                    'method'            : 'read',
                    'repositoryTargetId': 'any',
                    'repositoryId'      : '',
                    'repositoryGroupId' : ''
                ]
            ),
            new CPrivilege(
                id: 'repository-any-update', // T10
                type: 'target',
                name: 'All Repositories - (update)',
                description: 'Give permission to update any content in any repository.',
                properties: [
                    'method'            : 'update,read',
                    'repositoryTargetId': 'any',
                    'repositoryId'      : '',
                    'repositoryGroupId' : ''
                ]
            ),
            new CPrivilege(
                id: 'repository-any-create', // T11
                type: 'target',
                name: 'All Repositories - (create)',
                description: 'Give permission to create any content in any repository.',
                properties: [
                    'method'            : 'create,read',
                    'repositoryTargetId': 'any',
                    'repositoryId'      : '',
                    'repositoryGroupId' : ''
                ]
            ),
            new CPrivilege(
                id: 'repository-any-delete', // T12
                type: 'target',
                name: 'All Repositories - (delete)',
                description: 'Give permission to delete any content in any repository.',
                properties: [
                    'method'            : 'delete,read',
                    'repositoryTargetId': 'any',
                    'repositoryId'      : '',
                    'repositoryGroupId' : ''
                ]
            ),
            new CPrivilege(
                id: 'admin', // 1000
                type: 'method',
                name: 'Administrator privilege (ALL)',
                description: 'Give permission to everything available in nexus.',
                properties: [
                    'method'    : '*',
                    'permission': 'nexus:*'
                ]
            ),
            new CPrivilege(
                id: 'status', // 1
                type: 'method',
                name: 'Status - (read)',
                description: 'Give permission to query the nexus server for it\'s status.  This privilege is required by the anonymous user so that the UI can retrieve anonymous permissions on startup.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:status'
                ]
            ),
            new CPrivilege(
                id: 'signin', // 2
                type: 'method',
                name: 'Sign-in',
                description: 'Give permission to allow a user to sign-in to nexus.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:authentication'
                ]
            ),
            new CPrivilege(
                id: 'settings-read', // 3
                type: 'method',
                name: 'Server Settings - (read)',
                description: 'Give permission to read the nexus server settings.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:settings'
                ]
            ),
            new CPrivilege(
                id: 'settings-update', // 4
                type: 'method',
                name: 'Server Settings - (update,read)',
                description: 'Give permission to update,read the nexus server settings.',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:settings'
                ]
            ),
            new CPrivilege(
                id: 'repositories-create', // 5
                type: 'method',
                name: 'Repositories - (create,read)',
                description: 'Give permission to create,read new repositories.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repositories-read', // 6
                type: 'method',
                name: 'Repositories - (read)',
                description: 'Give permission to read existing repository configuration.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repositories-update', // 7
                type: 'method',
                name: 'Repositories - (update,read)',
                description: 'Give permission to update,read existing repository configuration.',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repositories-delete', // 8
                type: 'method',
                name: 'Repositories - (delete,read)',
                description: 'Give permission to delete,read existing repositories.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'blobstores-create', // 5
                type: 'method',
                name: 'Blobstores - (create,read)',
                description: 'Give permission to create,read new blobstores.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:blobstores'
                ]
            ),
            new CPrivilege(
                id: 'blobstores-read', // 6
                type: 'method',
                name: 'Blobstores - (read)',
                description: 'Give permission to read existing blobstore configuration.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:blobstores'
                ]
            ),
            new CPrivilege(
                id: 'blobstores-delete', // 8
                type: 'method',
                name: 'Blobstores - (delete,read)',
                description: 'Give permission to delete,read existing blobstores.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:blobstores'
                ]
            ),
            new CPrivilege(
                id: 'repotemplates-create', // 9
                type: 'method',
                name: 'Repository Templates - (create,read)',
                description: 'Give permission to create,read new repository templates.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:repotemplates'
                ]
            ),
            new CPrivilege(
                id: 'repotemplates-read', // 10
                type: 'method',
                name: 'Repository Templates - (read)',
                description: 'Give permission to read existing repository template configuration.  This privilege is required to add a new Repository in the UI, as the default values are retrieved from the template on the server.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repotemplates'
                ]
            ),
            new CPrivilege(
                id: 'repotemplates-update', // 11
                type: 'method',
                name: 'Repository Templates - (update,read)',
                description: 'Give permission to update,read existing repository template configuration.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:repotemplates'
                ]
            ),
            new CPrivilege(
                id: 'repotemplates-delete', // 12
                type: 'method',
                name: 'Repository Templates - (delete,read)',
                description: 'Give permission to delete,read existing repository templates.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:repotemplates'
                ]
            ),
            new CPrivilege(
                id: 'repogroups-create', // 13
                type: 'method',
                name: 'Repository Groups - (create,read)',
                description: 'Give permission to create,read new repository groups.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:repogroups'
                ]
            ),
            new CPrivilege(
                id: 'repogroups-read', // 14
                type: 'method',
                name: 'Repository Groups - (read)',
                description: 'Give permission to read existing repository group configuration.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repogroups'
                ]
            ),
            new CPrivilege(
                id: 'repogroups-update', // 15
                type: 'method',
                name: 'Repository Groups - (update,read)',
                description: 'Give permission to update,read existing repository group configuration.',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:repogroups'
                ]
            ),
            new CPrivilege(
                id: 'repogroups-delete', // 16
                type: 'method',
                name: 'Repository Groups - (delete,read)',
                description: 'Give permission to delete,read existing repository groups.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:repogroups'
                ]
            ),
            new CPrivilege(
                id: 'attributes-delete', // 20
                type: 'method',
                name: 'Rebuild Repository Attributes',
                description: 'Give permission to rebuild the attributes of repository content.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:attributes'
                ]
            ),
            new CPrivilege(
                id: 'cache-delete', // 21
                type: 'method',
                name: 'Clear Repository Cache',
                description: 'Give permission to clear the content of a repositories not found cache.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:cache'
                ]
            ),
            new CPrivilege(
                id: 'routes-create', // 22
                type: 'method',
                name: 'Repository Routes - (create,read)',
                description: 'Give permission to create,read repository routes.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:routes'
                ]
            ),
            new CPrivilege(
                id: 'routes-read', // 23
                type: 'method',
                name: 'Repository Routes - (read)',
                description: 'Give permission to read existing repository route configuration.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:routes'
                ]
            ),
            new CPrivilege(
                id: 'routes-update', // 24
                type: 'method',
                name: 'Repository Routes - (update,read)',
                description: 'Give permission to update,read existing repository route configuration.',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:routes'
                ]
            ),
            new CPrivilege(
                id: 'routes-delete', // 25
                type: 'method',
                name: 'Repository Routes - (delete,read)',
                description: 'Give permission to delete,read existing repository routes.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:routes'
                ]
            ),
            new CPrivilege(
                id: 'tasks-create', // 26
                type: 'method',
                name: 'Scheduled Tasks - (create,read)',
                description: 'Give permission to create,read scheduled tasks.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasks-read', // 27
                type: 'method',
                name: 'Scheduled Tasks - (read)',
                description: 'Give permission to read existing scheduled task configuration.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasks-update', // 28
                type: 'method',
                name: 'Scheduled Tasks - (update,read)',
                description: 'Give permission to update,read existing scheduled task configuration.',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasks-delete', // 29
                type: 'method',
                name: 'Scheduled Tasks - (delete,read)',
                description: 'Give permission to delete,read existing scheduled tasks.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'logs-read', // 42
                type: 'method',
                name: 'Logs - (read)',
                description: 'Give permission to retrieve the nexus log files.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:logs'
                ]
            ),
            new CPrivilege(
                id: 'configuration-read', // 43
                type: 'method',
                name: 'Configuration - (read)',
                description: 'Give permission to retrieve the nexus configuration.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:configuration'
                ]
            ),
            new CPrivilege(
                id: 'targets-create', // 45
                type: 'method',
                name: 'Repository Targets - (create,read)',
                description: 'Give permission to create,read repository targets.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:targets'
                ]
            ),
            new CPrivilege(
                id: 'targets-read', // 46
                type: 'method',
                name: 'Repository Targets - (read)',
                description: 'Give permission to read existing repository target configuration.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:targets'
                ]
            ),
            new CPrivilege(
                id: 'targets-update', // 47
                type: 'method',
                name: 'Repository Targets - (update,read)',
                description: 'Give permission to update,read existing repository target configuration.',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:targets'
                ]
            ),
            new CPrivilege(
                id: 'targets-delete', // 48
                type: 'method',
                name: 'Repository Targets - (delete,read)',
                description: 'Give permission to delete,read existing repository targets.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:targets'
                ]
            ),
            new CPrivilege(
                id: 'wastebasket-read', // 50
                type: 'method',
                name: 'Wastebasket - (read)',
                description: 'Give permission to read the contents of the nexus trash.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:wastebasket'
                ]
            ),
            new CPrivilege(
                id: 'wastebasket-delete', // 51
                type: 'method',
                name: 'Wastebasket - (delete,read)',
                description: 'Give permission to delete,read the contents of the nexus trash.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:wastebasket'
                ]
            ),
            new CPrivilege(
                id: 'artifact-read', // 54
                type: 'method',
                name: 'Artifact Download',
                description: 'Give permission to download artifacts (using /artifact service, not the /content url).  The extents of this privilege are related to the allowed targets..  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:artifact'
                ]
            ),
            new CPrivilege(
                id: 'repostatus-read', // 55
                type: 'method',
                name: 'Read Repository Status',
                description: 'Give permission to retrieve the status of a repository.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repostatus'
                ]
            ),
            new CPrivilege(
                id: 'repostatus-update', // 56
                type: 'method',
                name: 'Update Repository Status',
                description: 'Give permission to update the status of a repository.',
                properties: [
                    'method'    : 'update',
                    'permission': 'nexus:repostatus'
                ]
            ),
            new CPrivilege(
                id: 'artifact-create', // 65
                type: 'method',
                name: 'Artifact Upload',
                description: 'Give permission to upload artifacts (using /artifact service, not the /content service).  The extents of this privilege are related to the allowed targets.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:artifact'
                ]
            ),
            new CPrivilege(
                id: 'repometa-read', // 67
                type: 'method',
                name: 'Repository Summary Info (read)',
                description: 'Give permission to read the repository summary information.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repometa'
                ]
            ),
            new CPrivilege(
                id: 'tasksrun', // 68
                type: 'method',
                name: 'Scheduled Tasks - (run,stop)',
                description: 'Give permission to run and stop existing scheduled tasks manually.',
                properties: [
                    'method'    : 'read,delete',
                    'permission': 'nexus:tasksrun'
                ]
            ),
            new CPrivilege(
                id: 'tasktypes-read', // 69
                type: 'method',
                name: 'Scheduled Task Types - (read)',
                description: 'Give permission to retrieve list of support task types available in nexus.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:tasktypes'
                ]
            ),
            new CPrivilege(
                id: 'contentclasses-read', // 70
                type: 'method',
                name: 'Repository Content Classes Component - (read)',
                description: 'Give permission to retrieve the list of repository content classes supported by nexus.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:componentscontentclasses'
                ]
            ),
            new CPrivilege(
                id: 'scheduletypes-read', // 71
                type: 'method',
                name: 'Scheduled Task Types Component - (read)',
                description: 'Give permission to retrieve list of support task types available in nexus.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:componentscheduletypes'
                ]
            ),
            new CPrivilege(
                id: 'realmtypes-read', // 73
                type: 'method',
                name: 'Realm Types Component - (read)',
                description: 'Give permission to retrieve list of support realms available in nexus.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:componentrealmtypes'
                ]
            ),
            new CPrivilege(
                id: 'repotypes-read', // 74
                type: 'method',
                name: 'Repository Types - (read)',
                description: 'Give permission to retrieve the list of repository types supported by nexus.',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:componentsrepotypes'
                ]
            ),
            new CPrivilege(
                id: 'maven-metadata-delete', // 76
                type: 'method',
                name: 'Rebuild Maven Metadata',
                description: 'Give permission to rebuild the maven metadata of repository content.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:metadata'
                ]
            ),
            new CPrivilege(
                id: 'logconfig-update', // 77
                type: 'method',
                name: 'Log Configuration - (read,update)',
                description: 'Give permission to read and update log configuration',
                properties: [
                    'method'    : 'read,update',
                    'permission': 'nexus:logconfig'
                ]
            ),
            new CPrivilege(
                id: 'repositorymirrors-read', // 78
                type: 'method',
                name: 'Repository Mirrors - (read)',
                description: 'Give permission to read repository mirror configuration',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repositorymirrors'
                ]
            ),
            new CPrivilege(
                id: 'repositorymirrors-create', // 79
                type: 'method',
                name: 'Repository Mirrors - (create,read)',
                description: 'Give permission to create repository mirror configuration',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:repositorymirrors'
                ]
            ),
            new CPrivilege(
                id: 'repositorypredefinedmirrors-read', // 81
                type: 'method',
                name: 'Repository Mirrors, Retrieve Predefined List - (read)',
                description: 'Give permission to retrieve predefined list of mirrors from the server',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repositorypredefinedmirrors'
                ]
            ),
            new CPrivilege(
                id: 'repositorymirrorsstatus-read', // 82
                type: 'method',
                name: 'Repository Mirrors, Retrieve Mirror Status - (read)',
                description: 'Give permission to retrieve mirror status from the server',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repositorymirrorsstatus'
                ]
            ),
            new CPrivilege(
                id: 'search', // 17
                type: 'method',
                name: 'Search Repositories',
                description: 'Give permission to perform searches of repository content.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:index'
                ]
            ),
            new CPrivilege(
                id: 'apikey-access', // 83
                type: 'method',
                name: 'API-Key Access',
                description: 'Give permission to use an API-Key to access the server.',
                properties: [
                    'method'    : '*',
                    'permission': 'apikey:access'
                ]
            ),
            new CPrivilege(
                id: 'security-admin', // 1001
                type: 'method',
                name: 'Security administrator privilege (ALL)',
                description: 'Give permission to everything security related.',
                properties: [
                    'method'    : '*',
                    'permission': 'security:*'
                ]
            ),
            new CPrivilege(
                id: 'privileges-create', // 30
                type: 'method',
                name: 'User Privileges - (create,read)',
                description: 'Give permission to create,read privileges.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: 'privileges-read', // 31
                type: 'method',
                name: 'User Privileges - (read)',
                description: 'Give permission to read existing privilege configuration.',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: 'privileges-update', // 32
                type: 'method',
                name: 'User Privileges - (update,read)',
                description: 'Give permission to update,read existing privilege configuration.',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: 'privileges-delete', // 33
                type: 'method',
                name: 'User Privileges - (delete,read)',
                description: 'Give permission to delete,read existing privileges.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: 'roles-create', // 34
                type: 'method',
                name: 'User Roles - (create,read)',
                description: 'Give permission to create,read user roles.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: 'roles-read', // 35
                type: 'method',
                name: 'User Roles - (read)',
                description: 'Give permission to read existing user role configuration.',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: 'roles-update', // 36
                type: 'method',
                name: 'User Roles - (update,read)',
                description: 'Give permission to update,read existing user role configuration.',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: 'roles-delete', // 37
                type: 'method',
                name: 'User Roles - (delete,read)',
                description: 'Give permission to delete,read existing user roles.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: 'users-create', // 38
                type: 'method',
                name: 'Users - (create,read)',
                description: 'Give permission to create,read users.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: 'users-read', // 39
                type: 'method',
                name: 'Users - (read)',
                description: 'Give permission to read existing user configuration.',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: 'users-update', // 40
                type: 'method',
                name: 'Users - (update,read)',
                description: 'Give permission to update,read existing user configuration.',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: 'users-delete', // 41
                type: 'method',
                name: 'Users - (delete,read)',
                description: 'Give permission to delete,read existing users.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: 'usersforgotpw', // 57
                type: 'method',
                name: 'User Forgot Password - (create,read)',
                description: 'Give permission to request that a password be generated an emailed to a certain user.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:usersforgotpw'
                ]
            ),
            new CPrivilege(
                id: 'usersforgotid', // 58
                type: 'method',
                name: 'User Forgot User Id - (create,read)',
                description: 'Give permission to request that a username be emailed to a certain email address.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:usersforgotid'
                ]
            ),
            new CPrivilege(
                id: 'usersresetpw', // 59
                type: 'method',
                name: 'User Reset Password - (delete,read)',
                description: 'Give permission to reset any user\'s password.',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'security:usersreset'
                ]
            ),
            new CPrivilege(
                id: 'userschangepw', // 64
                type: 'method',
                name: 'User Change Password - (create,read)',
                description: 'Give permission to change a user\'s password.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:userschangepw'
                ]
            ),
            new CPrivilege(
                id: 'userssetpw', // 72
                type: 'method',
                name: 'User Set Password - (create,read)',
                description: 'Give permission to set a user\'s password.',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:userssetpw'
                ]
            ),
            new CPrivilege(
                id: 'userlocatortypes-read', // 75
                type: 'method',
                name: 'User Locator Types Component - (read)',
                description: 'Give permission to retrieve the list of User Locator types supported by nexus.',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:componentsuserlocatortypes'
                ]
            ),
            new CPrivilege(
                id: 'privilegetypes-read', // 80
                type: 'method',
                name: 'User Privilege Types - (read)',
                description: 'Give permission to read existing privilege types.',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:privilegetypes'
                ]
            )
        ],
        roles: [
            new CRole(
                id: 'nx-admin',
                name: 'Nexus Administrator Role',
                description: 'Administration role for Nexus',
                privileges: ['admin', 'security-admin', 'apikey-access']
            ),
            new CRole(
                id: 'nx-deployment',
                name: 'Nexus Deployment Role',
                description: 'Deployment role for Nexus',
                privileges: ['apikey-access'],
                roles: ['anonymous', 'ui-basic']
            ),
            new CRole(
                id: 'anonymous',
                name: 'Nexus Anonymous Role',
                description: 'Anonymous role for Nexus',
                privileges: ['status', 'artifact-read', 'usersforgotpw', 'usersforgotid', 'contentclasses-read', 'repotypes-read'],
                roles: ['ui-repo-browser', 'ui-search']
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
                privileges: ['repositories-create', 'repositories-read', 'repositories-update', 'repositories-delete', 'repotemplates-read', 'repositorymirrors-read', 'repositorymirrors-create', 'repositorypredefinedmirrors-read', 'repositorymirrorsstatus-read', 'repotypes-read', 'repository-all'],
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
                privileges: ['targets-create', 'targets-read', 'targets-update', 'targets-delete', 'contentclasses-read', 'repotypes-read']
            ),
            new CRole(
                id: 'ui-users-admin',
                name: 'UI: User Administration',
                description: 'Gives access to the User Administration screen in Nexus UI',
                privileges: ['roles-read', 'users-create', 'users-read', 'users-update', 'users-delete', 'userssetpw', 'userlocatortypes-read']
            ),
            new CRole(
                id: 'ui-roles-admin',
                name: 'UI: Role Administration',
                description: 'Gives access to the Role Administration screen in Nexus UI',
                privileges: ['privileges-read', 'roles-create', 'roles-read', 'roles-update', 'roles-delete']
            ),
            new CRole(
                id: 'ui-privileges-admin',
                name: 'UI: Privilege Administration',
                description: 'Gives access to the Privilege Administration screen in Nexus UI',
                privileges: ['repositories-read', 'repogroups-read', 'privileges-create', 'privileges-read', 'privileges-update', 'privileges-delete', 'targets-read', 'privilegetypes-read']
            ),
            new CRole(
                id: 'ui-basic',
                name: 'UI: Base UI Privileges',
                description: 'Generic privileges for users in the Nexus UI',
                privileges: ['status', 'signin', 'userschangepw']
            ),
            new CRole(
                id: 'ui-search',
                name: 'UI: Search',
                description: 'Gives access to the Search screen in Nexus UI',
                privileges: ['search', 'artifact-read']
            ),
            new CRole(
                id: 'nx-apikey-access',
                name: 'Nexus API-Key Access',
                description: 'API-Key Access role for Nexus.',
                privileges: ['apikey-access']
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

