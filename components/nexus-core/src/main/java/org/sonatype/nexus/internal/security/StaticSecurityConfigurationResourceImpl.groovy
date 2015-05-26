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
                type: 'wildcard',
                properties: [
                    pattern: 'nexus:*'
                ]
            ),

            //
            // nexus:settings
            //

            new CPrivilege(
                id: 'settings-all',
                type: 'application',
                properties: [
                    domain : 'settings',
                    actions: '*'
                ]
            ),
            new CPrivilege(
                id: 'settings-read',
                type: 'application',
                properties: [
                    domain : 'settings',
                    actions: 'read'
                ]
            ),
            new CPrivilege(
                id: 'settings-update',
                type: 'application',
                properties: [
                    domain : 'settings',
                    actions: 'update,read'
                ]
            ),

            //
            // nexus:bundles
            //

            new CPrivilege(
                id: 'bundles-all',
                type: 'application',
                properties: [
                    domain : 'bundles',
                    actions: '*'
                ]
            ),
            new CPrivilege(
                id: 'bundles-read',
                type: 'application',
                properties: [
                    domain : 'bundles',
                    actions: 'read'
                ]
            ),

            //
            // nexus:search
            //

            new CPrivilege(
                id: 'search-read',
                type: 'application',
                properties: [
                    domain : 'search',
                    actions: 'read'
                ]
            ),

            //
            // nexus:apikey
            //

            new CPrivilege(
                id: 'apikey-all',
                type: 'application',
                properties: [
                    domain : 'apikey',
                    actions: '*'
                ]
            ),

            //
            // nexus:privileges
            //

            new CPrivilege(
                id: 'privileges-all',
                type: 'application',
                properties: [
                    domain : 'privileges',
                    actions: '*'
                ]
            ),
            new CPrivilege(
                id: 'privileges-create',
                type: 'application',
                properties: [
                    domain : 'privileges',
                    actions: 'create,read'
                ]
            ),
            new CPrivilege(
                id: 'privileges-read',
                type: 'application',
                properties: [
                    domain : 'privileges',
                    actions: 'read'
                ]
            ),
            new CPrivilege(
                id: 'privileges-update',
                type: 'application',
                properties: [
                    domain : 'privileges',
                    actions: 'update,read'
                ]
            ),
            new CPrivilege(
                id: 'privileges-delete',
                type: 'application',
                properties: [
                    domain : 'privileges',
                    actions: 'delete,read'
                ]
            ),

            //
            // nexus:roles
            //

            new CPrivilege(
                id: 'roles-all',
                type: 'application',
                properties: [
                    domain : 'roles',
                    actions: '*'
                ]
            ),
            new CPrivilege(
                id: 'roles-create',
                type: 'application',
                properties: [
                    domain : 'roles',
                    actions: 'create,read'
                ]
            ),
            new CPrivilege(
                id: 'roles-read',
                type: 'application',
                properties: [
                    domain : 'roles',
                    actions: 'read'
                ]
            ),
            new CPrivilege(
                id: 'roles-update',
                type: 'application',
                properties: [
                    domain : 'roles',
                    actions: 'update,read'
                ]
            ),
            new CPrivilege(
                id: 'roles-delete',
                type: 'application',
                properties: [
                    domain : 'roles',
                    actions: 'delete,read'
                ]
            ),

            //
            // nexus:users
            //

            new CPrivilege(
                id: 'users-all',
                type: 'application',
                properties: [
                    domain : 'users',
                    actions: '*'
                ]
            ),
            new CPrivilege(
                id: 'users-create',
                type: 'application',
                properties: [
                    domain : 'users',
                    actions: 'create,read'
                ]
            ),
            new CPrivilege(
                id: 'users-read',
                type: 'application',
                properties: [
                    domain : 'users',
                    actions: 'read'
                ]
            ),
            new CPrivilege(
                id: 'users-update',
                type: 'application',
                properties: [
                    domain : 'users',
                    actions: 'update,read'
                ]
            ),
            new CPrivilege(
                id: 'users-delete',
                type: 'application',
                properties: [
                    domain : 'users',
                    actions: 'delete,read'
                ]
            ),

            // FIXME: Sort out what the use-case is for this distinct permission, consider nexus:users:change-password?
            new CPrivilege(
                id: 'userschangepw',
                type: 'application',
                properties: [
                    domain : 'userschangepw',
                    actions: 'create,read'
                ]
            )
        ],

        roles: [
            /**
             * Admin role grants all permissions (ie. super-user)
             */
            new CRole(
                id: 'admin',
                privileges: [
                    'all'
                ]
            ),

            /**
             * Anonymous role grants permissions to non-authenticated users.
             */
            new CRole(
                id: 'anonymous',

                privileges: [
                    'search-read',
                    'repository-view-*-*-browse',
                    'repository-view-*-*-read'
                ]
            )
        ]
    )
  }
}

