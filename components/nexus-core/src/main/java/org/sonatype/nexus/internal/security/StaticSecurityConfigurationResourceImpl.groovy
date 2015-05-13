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

import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.security.config.CPrivilege
import org.sonatype.nexus.security.config.CRole
import org.sonatype.nexus.security.config.MemorySecurityConfiguration
import org.sonatype.nexus.security.config.StaticSecurityConfigurationResource

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
                id: 'admin', // 1000
                type: 'method',
                properties: [
                    'method'    : '*',
                    'permission': 'nexus:*'
                ]
            ),
            new CPrivilege(
                id: 'status', // 1
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:status'
                ]
            ),
            new CPrivilege(
                id: 'signin', // 2
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:authentication'
                ]
            ),
            new CPrivilege(
                id: 'settings-read', // 3
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:settings'
                ]
            ),
            new CPrivilege(
                id: 'settings-update', // 4
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:settings'
                ]
            ),
            new CPrivilege(
                id: 'repositories-create', // 5
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repositories-read', // 6
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repositories-update', // 7
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repositories-delete', // 8
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repotemplates-create', // 9
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:repotemplates'
                ]
            ),
            new CPrivilege(
                id: 'repotemplates-read', // 10
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repotemplates'
                ]
            ),
            new CPrivilege(
                id: 'repotemplates-update', // 11
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:repotemplates'
                ]
            ),
            new CPrivilege(
                id: 'repotemplates-delete', // 12
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:repotemplates'
                ]
            ),
            new CPrivilege(
                id: 'repogroups-create', // 13
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:repogroups'
                ]
            ),
            new CPrivilege(
                id: 'repogroups-read', // 14
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repogroups'
                ]
            ),
            new CPrivilege(
                id: 'repogroups-update', // 15
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:repogroups'
                ]
            ),
            new CPrivilege(
                id: 'repogroups-delete', // 16
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:repogroups'
                ]
            ),
            new CPrivilege(
                id: 'attributes-delete', // 20
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:attributes'
                ]
            ),
            new CPrivilege(
                id: 'cache-delete', // 21
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:cache'
                ]
            ),
            new CPrivilege(
                id: 'routes-create', // 22
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:routes'
                ]
            ),
            new CPrivilege(
                id: 'routes-read', // 23
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:routes'
                ]
            ),
            new CPrivilege(
                id: 'routes-update', // 24
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:routes'
                ]
            ),
            new CPrivilege(
                id: 'routes-delete', // 25
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:routes'
                ]
            ),
            new CPrivilege(
                id: 'tasks-create', // 26
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasks-read', // 27
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasks-update', // 28
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasks-delete', // 29
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'logs-read', // 42
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:logs'
                ]
            ),
            new CPrivilege(
                id: 'logs-update',
                type: 'method',
                properties: [
                    'method'    : 'update',
                    'permission': 'nexus:logs'
                ]
            ),
            new CPrivilege(
                id: 'configuration-read', // 43
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:configuration'
                ]
            ),
            new CPrivilege(
                id: 'targets-create', // 45
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:targets'
                ]
            ),
            new CPrivilege(
                id: 'targets-read', // 46
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:targets'
                ]
            ),
            new CPrivilege(
                id: 'targets-update', // 47
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:targets'
                ]
            ),
            new CPrivilege(
                id: 'targets-delete', // 48
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:targets'
                ]
            ),
            new CPrivilege(
                id: 'wastebasket-read', // 50
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:wastebasket'
                ]
            ),
            new CPrivilege(
                id: 'wastebasket-delete', // 51
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:wastebasket'
                ]
            ),
            new CPrivilege(
                id: 'artifact-read', // 54
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:artifact'
                ]
            ),
            new CPrivilege(
                id: 'repostatus-read', // 55
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repostatus'
                ]
            ),
            new CPrivilege(
                id: 'repostatus-update', // 56
                type: 'method',
                properties: [
                    'method'    : 'update',
                    'permission': 'nexus:repostatus'
                ]
            ),
            new CPrivilege(
                id: 'artifact-create', // 65
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:artifact'
                ]
            ),
            new CPrivilege(
                id: 'repometa-read', // 67
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repometa'
                ]
            ),
            new CPrivilege(
                id: 'tasksrun', // 68
                type: 'method',
                properties: [
                    'method'    : 'read,delete',
                    'permission': 'nexus:tasksrun'
                ]
            ),
            new CPrivilege(
                id: 'tasktypes-read', // 69
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:tasktypes'
                ]
            ),
            new CPrivilege(
                id: 'contentclasses-read', // 70
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:componentscontentclasses'
                ]
            ),
            new CPrivilege(
                id: 'scheduletypes-read', // 71
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:componentscheduletypes'
                ]
            ),
            new CPrivilege(
                id: 'realmtypes-read', // 73
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:componentrealmtypes'
                ]
            ),
            new CPrivilege(
                id: 'repotypes-read', // 74
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:componentsrepotypes'
                ]
            ),
            new CPrivilege(
                id: 'maven-metadata-delete', // 76
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:metadata'
                ]
            ),
            new CPrivilege(
                id: 'logconfig-update', // 77
                type: 'method',
                properties: [
                    'method'    : 'read,update',
                    'permission': 'nexus:logconfig'
                ]
            ),
            new CPrivilege(
                id: 'repositorymirrors-read', // 78
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repositorymirrors'
                ]
            ),
            new CPrivilege(
                id: 'repositorymirrors-create', // 79
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:repositorymirrors'
                ]
            ),
            new CPrivilege(
                id: 'repositorypredefinedmirrors-read', // 81
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repositorypredefinedmirrors'
                ]
            ),
            new CPrivilege(
                id: 'repositorymirrorsstatus-read', // 82
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:repositorymirrorsstatus'
                ]
            ),
            new CPrivilege(
                id: 'search', // 17
                type: 'method',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:index'
                ]
            ),
            new CPrivilege(
                id: 'apikey-access', // 83
                type: 'method',
                properties: [
                    'method'    : '*',
                    'permission': 'apikey:access'
                ]
            ),
            new CPrivilege(
                id: 'security-admin', // 1001
                type: 'method',
                properties: [
                    'method'    : '*',
                    'permission': 'security:*'
                ]
            ),
            new CPrivilege(
                id: 'privileges-create', // 30
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: 'privileges-read', // 31
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: 'privileges-update', // 32
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: 'privileges-delete', // 33
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: 'roles-create', // 34
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: 'roles-read', // 35
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: 'roles-update', // 36
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: 'roles-delete', // 37
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: 'users-create', // 38
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: 'users-read', // 39
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: 'users-update', // 40
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: 'users-delete', // 41
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: 'usersforgotpw', // 57
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:usersforgotpw'
                ]
            ),
            new CPrivilege(
                id: 'usersforgotid', // 58
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:usersforgotid'
                ]
            ),
            new CPrivilege(
                id: 'userschangepw', // 64
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:userschangepw'
                ]
            ),
            new CPrivilege(
                id: 'userssetpw', // 72
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:userssetpw'
                ]
            ),
            new CPrivilege(
                id: 'userlocatortypes-read', // 75
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:componentsuserlocatortypes'
                ]
            ),
            new CPrivilege(
                id: 'privilegetypes-read', // 80
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:privilegetypes'
                ]
            )
        ],
        roles: [
            new CRole(
                id: 'nx-admin',
                privileges: ['admin', 'security-admin', 'apikey-access']
            ),
            new CRole(
                id: 'nx-deployment',
                privileges: ['apikey-access'],
                roles: ['anonymous', 'ui-basic']
            ),
            new CRole(
                id: 'anonymous',
                privileges: ['status', 'artifact-read', 'usersforgotpw', 'usersforgotid', 'contentclasses-read', 'repotypes-read'],
                roles: ['ui-repo-browser', 'ui-search']
            ),
            new CRole(
                id: 'nx-developer',
                roles: ['ui-basic', 'nx-deployment']
            ),
            new CRole(
                id: 'repo-all-read',
                privileges: ['repository-m2-read', 'repository-m1-read', 'repository-all']
            ),
            new CRole(
                id: 'repo-all-full',
                privileges: ['repository-m2-read', 'repository-m1-read', 'repository-m2-update', 'repository-m1-update', 'repository-m2-create', 'repository-m1-create', 'repository-m2-delete', 'repository-m1-delete', 'repository-all']
            ),
            new CRole(
                id: 'repository-any-read',
                privileges: ['repository-any-read', 'repository-all']
            ),
            new CRole(
                id: 'repository-any-full',
                privileges: ['repository-any-read', 'repository-any-update', 'repository-any-create', 'repository-any-delete', 'repository-all']
            ),
            new CRole(
                id: 'ui-repo-browser',
                privileges: ['repositories-read', 'repogroups-read', 'repostatus-read']
            ),
            new CRole(
                id: 'ui-logs-config-files',
                privileges: ['logs-read', 'configuration-read']
            ),
            new CRole(
                id: 'ui-server-admin',
                privileges: ['settings-read', 'settings-update', 'realmtypes-read']
            ),
            new CRole(
                id: 'ui-repository-admin',
                privileges: ['repositories-create', 'repositories-read', 'repositories-update', 'repositories-delete', 'repotemplates-read', 'repositorymirrors-read', 'repositorymirrors-create', 'repositorypredefinedmirrors-read', 'repositorymirrorsstatus-read', 'repotypes-read', 'repository-all'],
                roles: ['ui-repo-browser']
            ),
            new CRole(
                id: 'ui-group-admin',
                privileges: ['repositories-read', 'repogroups-create', 'repogroups-read', 'repogroups-update', 'repogroups-delete', 'repository-all'],
                roles: ['ui-repo-browser']
            ),
            new CRole(
                id: 'ui-routing-admin',
                privileges: ['repositories-read', 'repogroups-read', 'routes-create', 'routes-read', 'routes-update', 'routes-delete']
            ),
            new CRole(
                id: 'ui-scheduled-tasks-admin',
                privileges: ['repositories-read', 'repogroups-read', 'tasks-create', 'tasks-read', 'tasks-update', 'tasks-delete', 'tasksrun', 'tasktypes-read', 'scheduletypes-read']
            ),
            new CRole(
                id: 'ui-repository-targets-admin',
                privileges: ['targets-create', 'targets-read', 'targets-update', 'targets-delete', 'contentclasses-read', 'repotypes-read']
            ),
            new CRole(
                id: 'ui-users-admin',
                privileges: ['roles-read', 'users-create', 'users-read', 'users-update', 'users-delete', 'userssetpw', 'userlocatortypes-read']
            ),
            new CRole(
                id: 'ui-roles-admin',
                privileges: ['privileges-read', 'roles-create', 'roles-read', 'roles-update', 'roles-delete']
            ),
            new CRole(
                id: 'ui-privileges-admin',
                privileges: ['repositories-read', 'repogroups-read', 'privileges-create', 'privileges-read', 'privileges-update', 'privileges-delete', 'targets-read', 'privilegetypes-read']
            ),
            new CRole(
                id: 'ui-basic',
                privileges: ['status', 'signin', 'userschangepw']
            ),
            new CRole(
                id: 'ui-search',
                privileges: ['search', 'artifact-read']
            ),
            new CRole(
                id: 'nx-apikey-access',
                privileges: ['apikey-access']
            )
        ]
    )
  }
}

