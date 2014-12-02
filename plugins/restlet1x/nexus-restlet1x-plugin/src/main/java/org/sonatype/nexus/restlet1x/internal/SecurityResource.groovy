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
package org.sonatype.nexus.restlet1x.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Restlet plugin static security resource.
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
                id: '1001',
                type: 'method',
                name: 'Security administrator privilege (ALL)',
                description: 'Give permission to everything security related.',
                properties: [
                    'method': '*',
                    'permission': 'security:*'
                ]
            ),
            new CPrivilege(
                id: '30',
                type: 'method',
                name: 'User Privileges - (create,read)',
                description: 'Give permission to create,read privileges.',
                properties: [
                    'method': 'create,read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: '31',
                type: 'method',
                name: 'User Privileges - (read)',
                description: 'Give permission to read existing privilege configuration.',
                properties: [
                    'method': 'read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: '32',
                type: 'method',
                name: 'User Privileges - (update,read)',
                description: 'Give permission to update,read existing privilege configuration.',
                properties: [
                    'method': 'update,read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: '33',
                type: 'method',
                name: 'User Privileges - (delete,read)',
                description: 'Give permission to delete,read existing privileges.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'security:privileges'
                ]
            ),
            new CPrivilege(
                id: '34',
                type: 'method',
                name: 'User Roles - (create,read)',
                description: 'Give permission to create,read user roles.',
                properties: [
                    'method': 'create,read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: '35',
                type: 'method',
                name: 'User Roles - (read)',
                description: 'Give permission to read existing user role configuration.',
                properties: [
                    'method': 'read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: '36',
                type: 'method',
                name: 'User Roles - (update,read)',
                description: 'Give permission to update,read existing user role configuration.',
                properties: [
                    'method': 'update,read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: '37',
                type: 'method',
                name: 'User Roles - (delete,read)',
                description: 'Give permission to delete,read existing user roles.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'security:roles'
                ]
            ),
            new CPrivilege(
                id: '38',
                type: 'method',
                name: 'Users - (create,read)',
                description: 'Give permission to create,read users.',
                properties: [
                    'method': 'create,read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: '39',
                type: 'method',
                name: 'Users - (read)',
                description: 'Give permission to read existing user configuration.',
                properties: [
                    'method': 'read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: '40',
                type: 'method',
                name: 'Users - (update,read)',
                description: 'Give permission to update,read existing user configuration.',
                properties: [
                    'method': 'update,read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: '41',
                type: 'method',
                name: 'Users - (delete,read)',
                description: 'Give permission to delete,read existing users.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'security:users'
                ]
            ),
            new CPrivilege(
                id: '57',
                type: 'method',
                name: 'User Forgot Password - (create,read)',
                description: 'Give permission to request that a password be generated an emailed to a certain user.',
                properties: [
                    'method': 'create,read',
                    'permission': 'security:usersforgotpw'
                ]
            ),
            new CPrivilege(
                id: '58',
                type: 'method',
                name: 'User Forgot User Id - (create,read)',
                description: 'Give permission to request that a username be emailed to a certain email address.',
                properties: [
                    'method': 'create,read',
                    'permission': 'security:usersforgotid'
                ]
            ),
            new CPrivilege(
                id: '59',
                type: 'method',
                name: 'User Reset Password - (delete,read)',
                description: 'Give permission to reset any user\'s password.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'security:usersreset'
                ]
            ),
            new CPrivilege(
                id: '64',
                type: 'method',
                name: 'User Change Password - (create,read)',
                description: 'Give permission to change a user\'s password.',
                properties: [
                    'method': 'create,read',
                    'permission': 'security:userschangepw'
                ]
            ),
            new CPrivilege(
                id: '72',
                type: 'method',
                name: 'User Set Password - (create,read)',
                description: 'Give permission to set a user\'s password.',
                properties: [
                    'method': 'create,read',
                    'permission': 'security:userssetpw'
                ]
            ),
            new CPrivilege(
                id: '75',
                type: 'method',
                name: 'User Locator Types Component - (read)',
                description: 'Give permission to retrieve the list of User Locator types supported by nexus.',
                properties: [
                    'method': 'read',
                    'permission': 'security:componentsuserlocatortypes'
                ]
            ),
            new CPrivilege(
                id: '80',
                type: 'method',
                name: 'User Privilege Types - (read)',
                description: 'Give permission to read existing privilege types.',
                properties: [
                    'method': 'read',
                    'permission': 'security:privilegetypes'
                ]
            )
        ]
    )
  }
}

