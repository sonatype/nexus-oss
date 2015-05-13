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
            /**
             * Grants permission for anything in the 'nexus:' namespace.
             */
            new CPrivilege(
                id: 'all',
                type: 'method',
                properties: [
                    'method'    : '*',
                    'permission': 'nexus'
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
                id: 'admin',
                privileges: [
                    'all',
                    // FIXME: These don't start with "nexus:*"
                    'security-admin',
                    'apikey-access'
                ]
            ),
            new CRole(
                id: 'anonymous',
                privileges: [
                    'status'
                ]
            )
        ]
    )
  }
}

