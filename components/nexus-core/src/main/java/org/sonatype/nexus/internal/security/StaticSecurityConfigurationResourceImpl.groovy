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
                id: 'repositories-update',
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:repositories'
                ]
            ),
            new CPrivilege(
                id: 'repositories-delete',
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:repositories'
                ]
            ),

            // TODO: nexus:tasks used by UI, refactor
            new CPrivilege(
                id: 'tasks-create',
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasks-read',
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasks-update',
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasks-delete',
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'nexus:tasks'
                ]
            ),
            new CPrivilege(
                id: 'tasksrun',
                type: 'method',
                properties: [
                    'method'    : 'read,delete',
                    'permission': 'nexus:tasksrun'
                ]
            ),
            new CPrivilege(
                id: 'tasktypes-read',
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:tasktypes'
                ]
            ),

            // TODO: nexus:logs used by UI, refactor
            new CPrivilege(
                id: 'logs-read',
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
                id: 'logconfig-update',
                type: 'method',
                properties: [
                    'method'    : 'read,update',
                    'permission': 'nexus:logconfig'
                ]
            ),

            // TODO: Used by RealmSettingsComponent
            new CPrivilege(
                id: 'realmtypes-read',
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'nexus:componentrealmtypes'
                ]
            ),

            new CPrivilege(
                id: 'apikey-all',
                type: 'method',
                properties: [
                    'method'    : '*',
                    'permission': 'nexus:apikey'
                ]
            ),

            // FIXME: change namespace to use nexus:
            new CPrivilege(
                id: 'security-admin',
                type: 'method',
                properties: [
                    'method'    : '*',
                    'permission': 'security:*'
                ]
            ),
            new CPrivilege(
                id: 'privileges-create',
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: 'privileges-read',
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: 'privileges-update',
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: 'privileges-delete',
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: 'roles-create',
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: 'roles-read',
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: 'roles-update',
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: 'roles-delete',
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: 'users-create',
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: 'users-read',
                type: 'method',
                properties: [
                    'method'    : 'read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: 'users-update',
                type: 'method',
                properties: [
                    'method'    : 'update,read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: 'users-delete',
                type: 'method',
                properties: [
                    'method'    : 'delete,read',
                    'permission': 'security:users'
                ]
            ),

            // FIXME: Normalize name scheme
            new CPrivilege(
                id: 'userschangepw',
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:userschangepw'
                ]
            ),

            // FIXME: Normalize name scheme
            new CPrivilege(
                id: 'userssetpw',
                type: 'method',
                properties: [
                    'method'    : 'create,read',
                    'permission': 'security:userssetpw'
                ]
            )
        ],
        roles: [
            new CRole(
                id: 'admin',
                privileges: [
                    'all',
                    // FIXME: These don't start with "nexus:*", once that is fixed can be removed
                    'security-admin'
                ]
            ),
            new CRole(
                id: 'anonymous',
                privileges: [
                    // FIXME: Sort out what we need for defaults
                    'status'
                ]
            )
        ]
    )
  }
}

