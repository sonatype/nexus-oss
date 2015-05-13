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
                id: 'status',
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:status'
                ]
            ),
            new CPrivilege(
                id: 'signin',
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:authentication'
                ]
            ),
            new CPrivilege(
                id: 'settings-read',
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:settings'
                ]
            ),
            new CPrivilege(
                id: 'settings-update',
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:settings'
                ]
            ),

            // TODO: nexus:repositories used by UI, refactor
            new CPrivilege(
                id: 'repositories-create',
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repositories-read',
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

            // TODO: nexus:tasks used by UI, refactor
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

            // TODO: nexus:logs used by UI, refactor
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

            // TODO: nexus:tasksrun used by UI, refactor
            new CPrivilege(
                id: 'tasksrun', // 68
                type: 'method',
                properties: [
                    'method'    : 'read,delete',
                    'permission': 'nexus:tasksrun'
                ]
            ),

            // TODO: nexus:tasktypes used by UI, refactor
            new CPrivilege(
                id: 'tasktypes-read', // 69
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:tasktypes'
                ]
            ),

            // TODO: Used by RealmSettingsComponent
            new CPrivilege(
                id: 'realmtypes-read', // 73
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:componentrealmtypes'
                ]
            ),

            // TODO: nexus:logconfig used by UI, refactor
            new CPrivilege(
                id: 'logconfig-update', // 77
                type: 'method',
                properties: [
                    'method'    : 'read,update',
                    'permission': 'nexus:logconfig'
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

