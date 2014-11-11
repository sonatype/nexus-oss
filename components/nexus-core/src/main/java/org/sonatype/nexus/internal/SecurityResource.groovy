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
import org.sonatype.security.model.CProperty
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
                    new CProperty(key: 'method', value: '*'),
                    new CProperty(key: 'permission', value: 'nexus:metrics-endpoints')
                ]
            ),
            new CPrivilege(
                id: 'T1',
                type: 'target',
                name: 'All M2 Repositories - (read)',
                description: 'Give permission to read any content in any Maven 2 type repositories.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'repositoryTargetId', value: '1'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ]
            ),
            new CPrivilege(
                id: 'T2',
                type: 'target',
                name: 'All M1 Repositories - (read)',
                description: 'Give permission to read any content in any Maven 1 type repositories.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'repositoryTargetId', value: '2'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ]
            ),
            new CPrivilege(
                id: 'T3',
                type: 'target',
                name: 'All M2 Repositories - (update)',
                description: 'Give permission to update any content in any Maven 2 type repositories.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'repositoryTargetId', value: '1'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ]
            ),
            new CPrivilege(
                id: 'T4',
                type: 'target',
                name: 'All M1 Repositories - (update)',
                description: 'Give permission to update any content in any Maven 1 type repositories.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'repositoryTargetId', value: '2'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ]
            ),
            new CPrivilege(
                id: 'T5',
                type: 'target',
                name: 'All M2 Repositories - (create)',
                description: 'Give permission to create any content in any Maven 2 type repositories.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'repositoryTargetId', value: '1'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ]
            ),
            new CPrivilege(
                id: 'T6',
                type: 'target',
                name: 'All M1 Repositories - (create)',
                description: 'Give permission to create any content in any Maven 1 type repositories.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'repositoryTargetId', value: '2'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ]
            ),
            new CPrivilege(
                id: 'T7',
                type: 'target',
                name: 'All M2 Repositories - (delete)',
                description: 'Give permission to delete any content in any Maven 2 type repositories.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'repositoryTargetId', value: '1'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ]
            ),
            new CPrivilege(
                id: 'T8',
                type: 'target',
                name: 'All M1 Repositories - (delete)',
                description: 'Give permission to delete any content in any Maven 1 type repositories.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'repositoryTargetId', value: '2'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ]
            ),
            new CPrivilege(
                id: 'T9',
                type: 'target',
                name: 'All Repositories - (read)',
                description: 'Give permission to read any content in any repository.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'repositoryTargetId', value: 'any'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ]
            ),
            new CPrivilege(
                id: 'T10',
                type: 'target',
                name: 'All Repositories - (update)',
                description: 'Give permission to update any content in any repository.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'repositoryTargetId', value: 'any'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ]
            ),
            new CPrivilege(
                id: 'T11',
                type: 'target',
                name: 'All Repositories - (create)',
                description: 'Give permission to create any content in any repository.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'repositoryTargetId', value: 'any'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ]
            ),
            new CPrivilege(
                id: 'T12',
                type: 'target',
                name: 'All Repositories - (delete)',
                description: 'Give permission to delete any content in any repository.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'repositoryTargetId', value: 'any'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ]
            ),
            new CPrivilege(
                id: '1000',
                type: 'method',
                name: 'Administrator privilege (ALL)',
                description: 'Give permission to everything available in nexus.',
                properties: [
                    new CProperty(key: 'method', value: '*'),
                    new CProperty(key: 'permission', value: 'nexus:*')
                ]
            ),
            new CPrivilege(
                id: '1',
                type: 'method',
                name: 'Status - (read)',
                description: 'Give permission to query the nexus server for it\'s status.  This privilege is required by the anonymous user so that the UI can retrieve anonymous permissions on startup.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:status')
                ]
            ),
            new CPrivilege(
                id: '2',
                type: 'method',
                name: 'Login to UI',
                description: 'Give permission to allow a user to login to nexus.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:authentication')
                ]
            ),
            new CPrivilege(
                id: '3',
                type: 'method',
                name: 'Server Settings - (read)',
                description: 'Give permission to read the nexus server settings.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:settings')
                ]
            ),
            new CPrivilege(
                id: '4',
                type: 'method',
                name: 'Server Settings - (update,read)',
                description: 'Give permission to update,read the nexus server settings.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'permission', value: 'nexus:settings')
                ]
            ),
            new CPrivilege(
                id: '5',
                type: 'method',
                name: 'Repositories - (create,read)',
                description: 'Give permission to create,read new repositories.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'permission', value: 'nexus:repositories')
                ]
            ),
            new CPrivilege(
                id: '6',
                type: 'method',
                name: 'Repositories - (read)',
                description: 'Give permission to read existing repository configuration.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:repositories')
                ]
            ),
            new CPrivilege(
                id: '7',
                type: 'method',
                name: 'Repositories - (update,read)',
                description: 'Give permission to update,read existing repository configuration.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'permission', value: 'nexus:repositories')
                ]
            ),
            new CPrivilege(
                id: '8',
                type: 'method',
                name: 'Repositories - (delete,read)',
                description: 'Give permission to delete,read existing repositories.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:repositories')
                ]
            ),
            new CPrivilege(
                id: '9',
                type: 'method',
                name: 'Repository Templates - (create,read)',
                description: 'Give permission to create,read new repository templates.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'permission', value: 'nexus:repotemplates')
                ]
            ),
            new CPrivilege(
                id: '10',
                type: 'method',
                name: 'Repository Templates - (read)',
                description: 'Give permission to read existing repository template configuration.  This privilege is required to add a new Repository in the UI, as the default values are retrieved from the template on the server.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:repotemplates')
                ]
            ),
            new CPrivilege(
                id: '11',
                type: 'method',
                name: 'Repository Templates - (update,read)',
                description: 'Give permission to update,read existing repository template configuration.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'permission', value: 'nexus:repotemplates')
                ]
            ),
            new CPrivilege(
                id: '12',
                type: 'method',
                name: 'Repository Templates - (delete,read)',
                description: 'Give permission to delete,read existing repository templates.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:repotemplates')
                ]
            ),
            new CPrivilege(
                id: '13',
                type: 'method',
                name: 'Repository Groups - (create,read)',
                description: 'Give permission to create,read new repository groups.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'permission', value: 'nexus:repogroups')
                ]
            ),
            new CPrivilege(
                id: '14',
                type: 'method',
                name: 'Repository Groups - (read)',
                description: 'Give permission to read existing repository group configuration.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:repogroups')
                ]
            ),
            new CPrivilege(
                id: '15',
                type: 'method',
                name: 'Repository Groups - (update,read)',
                description: 'Give permission to update,read existing repository group configuration.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'permission', value: 'nexus:repogroups')
                ]
            ),
            new CPrivilege(
                id: '16',
                type: 'method',
                name: 'Repository Groups - (delete,read)',
                description: 'Give permission to delete,read existing repository groups.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:repogroups')
                ]
            ),
            new CPrivilege(
                id: '20',
                type: 'method',
                name: 'Rebuild Repository Attributes',
                description: 'Give permission to rebuild the attributes of repository content.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:attributes')
                ]
            ),
            new CPrivilege(
                id: '21',
                type: 'method',
                name: 'Clear Repository Cache',
                description: 'Give permission to clear the content of a repositories not found cache.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:cache')
                ]
            ),
            new CPrivilege(
                id: '22',
                type: 'method',
                name: 'Repository Routes - (create,read)',
                description: 'Give permission to create,read repository routes.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'permission', value: 'nexus:routes')
                ]
            ),
            new CPrivilege(
                id: '23',
                type: 'method',
                name: 'Repository Routes - (read)',
                description: 'Give permission to read existing repository route configuration.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:routes')
                ]
            ),
            new CPrivilege(
                id: '24',
                type: 'method',
                name: 'Repository Routes - (update,read)',
                description: 'Give permission to update,read existing repository route configuration.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'permission', value: 'nexus:routes')
                ]
            ),
            new CPrivilege(
                id: '25',
                type: 'method',
                name: 'Repository Routes - (delete,read)',
                description: 'Give permission to delete,read existing repository routes.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:routes')
                ]
            ),
            new CPrivilege(
                id: '26',
                type: 'method',
                name: 'Scheduled Tasks - (create,read)',
                description: 'Give permission to create,read scheduled tasks.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'permission', value: 'nexus:tasks')
                ]
            ),
            new CPrivilege(
                id: '27',
                type: 'method',
                name: 'Scheduled Tasks - (read)',
                description: 'Give permission to read existing scheduled task configuration.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:tasks')
                ]
            ),
            new CPrivilege(
                id: '28',
                type: 'method',
                name: 'Scheduled Tasks - (update,read)',
                description: 'Give permission to update,read existing scheduled task configuration.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'permission', value: 'nexus:tasks')
                ]
            ),
            new CPrivilege(
                id: '29',
                type: 'method',
                name: 'Scheduled Tasks - (delete,read)',
                description: 'Give permission to delete,read existing scheduled tasks.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:tasks')
                ]
            ),
            new CPrivilege(
                id: '42',
                type: 'method',
                name: 'Logs - (read)',
                description: 'Give permission to retrieve the nexus log files.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:logs')
                ]
            ),
            new CPrivilege(
                id: '43',
                type: 'method',
                name: 'Configuration File - (read)',
                description: 'Give permission to retrieve the nexus.xml configuration file.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:configuration')
                ]
            ),
            new CPrivilege(
                id: '45',
                type: 'method',
                name: 'Repository Targets - (create,read)',
                description: 'Give permission to create,read repository targets.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'permission', value: 'nexus:targets')
                ]
            ),
            new CPrivilege(
                id: '46',
                type: 'method',
                name: 'Repository Targets - (read)',
                description: 'Give permission to read existing repository target configuration.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:targets')
                ]
            ),
            new CPrivilege(
                id: '47',
                type: 'method',
                name: 'Repository Targets - (update,read)',
                description: 'Give permission to update,read existing repository target configuration.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'permission', value: 'nexus:targets')
                ]
            ),
            new CPrivilege(
                id: '48',
                type: 'method',
                name: 'Repository Targets - (delete,read)',
                description: 'Give permission to delete,read existing repository targets.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:targets')
                ]
            ),
            new CPrivilege(
                id: '49',
                type: 'method',
                name: 'Server Status - (update,read)',
                description: 'Give permission to update,read the status of the nexus server.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'permission', value: 'nexus:status')
                ]
            ),
            new CPrivilege(
                id: '50',
                type: 'method',
                name: 'Wastebasket - (read)',
                description: 'Give permission to read the contents of the nexus trash.  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:wastebasket')
                ]
            ),
            new CPrivilege(
                id: '51',
                type: 'method',
                name: 'Wastebasket - (delete,read)',
                description: 'Give permission to delete,read the contents of the nexus trash.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:wastebasket')
                ]
            ),
            new CPrivilege(
                id: '54',
                type: 'method',
                name: 'Artifact Download',
                description: 'Give permission to download artifacts (using /artifact service, not the /content url).  The extents of this privilege are related to the allowed targets..  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:artifact')
                ]
            ),
            new CPrivilege(
                id: '55',
                type: 'method',
                name: 'Read Repository Status',
                description: 'Give permission to retrieve the status of a repository.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:repostatus')
                ]
            ),
            new CPrivilege(
                id: '56',
                type: 'method',
                name: 'Update Repository Status',
                description: 'Give permission to update the status of a repository.',
                properties: [
                    new CProperty(key: 'method', value: 'update'),
                    new CProperty(key: 'permission', value: 'nexus:repostatus')
                ]
            ),
            new CPrivilege(
                id: '65',
                type: 'method',
                name: 'Artifact Upload',
                description: 'Give permission to upload artifacts (using /artifact service, not the /content service).  The extents of this privilege are related to the allowed targets.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'permission', value: 'nexus:artifact')
                ]
            ),
            new CPrivilege(
                id: '66',
                type: 'method',
                name: 'Nexus Remote Control',
                description: 'Give permission to remotely control nexus server (start, stop, etc.).  Note that this privilege is not currently invoked by the Nexus UI.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'permission', value: 'nexus:command')
                ]
            ),
            new CPrivilege(
                id: '67',
                type: 'method',
                name: 'Repository Summary Info (read)',
                description: 'Give permission to read the repository summary information.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:repometa')
                ]
            ),
            new CPrivilege(
                id: '68',
                type: 'method',
                name: 'Scheduled Tasks - (run,stop)',
                description: 'Give permission to run and stop existing scheduled tasks manually.',
                properties: [
                    new CProperty(key: 'method', value: 'read,delete'),
                    new CProperty(key: 'permission', value: 'nexus:tasksrun')
                ]
            ),
            new CPrivilege(
                id: '69',
                type: 'method',
                name: 'Scheduled Task Types - (read)',
                description: 'Give permission to retrieve list of support task types available in nexus.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:tasktypes')
                ]
            ),
            new CPrivilege(
                id: '70',
                type: 'method',
                name: 'Repository Content Classes Component - (read)',
                description: 'Give permission to retrieve the list of repository content classes supported by nexus.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:componentscontentclasses')
                ]
            ),
            new CPrivilege(
                id: '71',
                type: 'method',
                name: 'Scheduled Task Types Component - (read)',
                description: 'Give permission to retrieve list of support task types available in nexus.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:componentscheduletypes')
                ]
            ),
            new CPrivilege(
                id: '73',
                type: 'method',
                name: 'Realm Types Component - (read)',
                description: 'Give permission to retrieve list of support realms available in nexus.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:componentrealmtypes')
                ]
            ),
            new CPrivilege(
                id: '74',
                type: 'method',
                name: 'Repository Types - (read)',
                description: 'Give permission to retrieve the list of repository types supported by nexus.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:componentsrepotypes')
                ]
            ),
            new CPrivilege(
                id: '76',
                type: 'method',
                name: 'Rebuild Maven Metadata',
                description: 'Give permission to rebuild the maven metadata of repository content.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:metadata')
                ]
            ),
            new CPrivilege(
                id: '77',
                type: 'method',
                name: 'Log Configuration - (read,update)',
                description: 'Give permission to read and update log configuration',
                properties: [
                    new CProperty(key: 'method', value: 'read,update'),
                    new CProperty(key: 'permission', value: 'nexus:logconfig')
                ]
            ),
            new CPrivilege(
                id: '78',
                type: 'method',
                name: 'Repository Mirrors - (read)',
                description: 'Give permission to read repository mirror configuration',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:repositorymirrors')
                ]
            ),
            new CPrivilege(
                id: '79',
                type: 'method',
                name: 'Repository Mirrors - (create,read)',
                description: 'Give permission to create repository mirror configuration',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'permission', value: 'nexus:repositorymirrors')
                ]
            ),
            new CPrivilege(
                id: '81',
                type: 'method',
                name: 'Repository Mirrors, Retrieve Predefined List - (read)',
                description: 'Give permission to retrieve predefined list of mirrors from the server',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:repositorypredefinedmirrors')
                ]
            ),
            new CPrivilege(
                id: '82',
                type: 'method',
                name: 'Repository Mirrors, Retrieve Mirror Status - (read)',
                description: 'Give permission to retrieve mirror status from the server',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:repositorymirrorsstatus')
                ]
            ),
            new CPrivilege(
                id: '83',
                type: 'method',
                name: 'API-Key Access',
                description: 'Give permission to use an API-Key to access the server.',
                properties: [
                    new CProperty(key: 'method', value: '*'),
                    new CProperty(key: 'permission', value: 'apikey:access')
                ]
            )
        ],
        roles: [
            new CRole(
                id: 'nx-admin',
                name: 'Nexus Administrator Role',
                description: 'Administration role for Nexus',
                privileges: ['1000', '1001', '83']
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
                privileges: ['1', '54', '57', '58', '70', '74'],
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
                privileges: ['T1', 'T2', 'repository-all']
            ),
            new CRole(
                id: 'repo-all-full',
                name: 'Repo: All Maven Repositories (Full Control)',
                description: 'Gives access to create/read/update/delete ALL content of ALL Maven1 and Maven2 repositories in Nexus.',
                privileges: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'repository-all']
            ),
            new CRole(
                id: 'repository-any-read',
                name: 'Repo: All Repositories (Read)',
                description: 'Gives access to read ALL content of ALL repositories in Nexus.',
                privileges: ['T9', 'repository-all']
            ),
            new CRole(
                id: 'repository-any-full',
                name: 'Repo: All Repositories (Full Control)',
                description: 'Gives access to create/read/update/delete ALL content of ALL repositories in Nexus.',
                privileges: ['T9', 'T10', 'T11', 'T12', 'repository-all']
            ),
            new CRole(
                id: 'ui-repo-browser',
                name: 'UI: Repository Browser',
                description: 'Gives access to the Repository Browser screen in Nexus UI',
                privileges: ['6', '14', '55']
            ),
            new CRole(
                id: 'ui-logs-config-files',
                name: 'UI: Logs and Config Files',
                description: 'Gives access to the Logs and Config Files screen in Nexus UI',
                privileges: ['42', '43']
            ),
            new CRole(
                id: 'ui-server-admin',
                name: 'UI: Server Administration',
                description: 'Gives access to the Server Administration screen in Nexus UI',
                privileges: ['3', '4', '73']
            ),
            new CRole(
                id: 'ui-repository-admin',
                name: 'UI: Repository Administration',
                description: 'Gives access to the Repository Administration screen in Nexus UI',
                privileges: ['5', '6', '7', '8', '10', '78', '79', '81', '82', '74', 'repository-all'],
                roles: ['ui-repo-browser']
            ),
            new CRole(
                id: 'ui-group-admin',
                name: 'UI: Group Administration',
                description: 'Gives access to the Group Administration screen in Nexus UI',
                privileges: ['6', '13', '14', '15', '16', 'repository-all'],
                roles: ['ui-repo-browser']
            ),
            new CRole(
                id: 'ui-routing-admin',
                name: 'UI: Routing Administration',
                description: 'Gives access to the Routing Administration screen in Nexus UI',
                privileges: ['6', '14', '22', '23', '24', '25']
            ),
            new CRole(
                id: 'ui-scheduled-tasks-admin',
                name: 'UI: Scheduled Task Administration',
                description: 'Gives access to the Scheduled Task Administration screen in Nexus UI',
                privileges: ['6', '14', '26', '27', '28', '29', '68', '69', '71']
            ),
            new CRole(
                id: 'ui-repository-targets-admin',
                name: 'UI: Repository Target Administration',
                description: 'Gives access to the Repository Target Administration screen in Nexus UI',
                privileges: ['45', '46', '47', '48', '70', '74']
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
                privileges: ['6', '14', '30', '31', '32', '33', '46', '80']
            ),
            new CRole(
                id: 'ui-basic',
                name: 'UI: Base UI Privileges',
                description: 'Generic privileges for users in the Nexus UI',
                privileges: ['1', '2', '64']
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

