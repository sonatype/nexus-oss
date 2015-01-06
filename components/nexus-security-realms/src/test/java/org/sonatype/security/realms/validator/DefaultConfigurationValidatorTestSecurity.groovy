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
package org.sonatype.security.realms.validator

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CRole
import org.sonatype.security.model.CUser
import org.sonatype.security.model.CUserRoleMapping
import org.sonatype.security.model.Configuration

/**
 * @since 3.0
 */
class DefaultConfigurationValidatorTestSecurity
{

  static Configuration securityModel1() {
    return new Configuration(
        users: [
            new CUser(
                id: ''
            ),
            new CUser(
                id: 'ausernostatus',
                password: 'fjsf8j4r3',
                firstName: 'Alex',
                lastName: 'User',
                email: 'auser@auser.com'
            ),
            new CUser(
                id: 'auser',
                password: 'fjsf8j4r3',
                firstName: 'Alex',
                lastName: 'User',
                status: 'active',
                email: 'auser@auser.com'
            ),
            new CUser(
                id: 'buser',
                password: 'fjsf8j4r3',
                firstName: 'Alex',
                lastName: 'User',
                status: 'active',
                email: 'unique@auser.com'
            )
        ],
        userRoleMappings: [
            new CUserRoleMapping(
                userId: '',
                source: '',
                roles: ['id1']
            ),
            new CUserRoleMapping(
                userId: '',
                source: 'default',
                roles: ['id1']
            ),
            new CUserRoleMapping(
                userId: 'buser',
                source: 'default',
                roles: ['id']
            )
        ],
        privileges: [
            new CPrivilege(
                id: '1',
                type: 'method',
                name: 'priv',
                properties: [
                    'method': 'read',
                    'permission': '/some/path/'
                ]
            )
        ],
        roles: [
            new CRole(
                id: 'id',
                name: 'name',
                privileges: ['1']
            )
        ]
    )
  }

  static Configuration securityModel2() {
    return new Configuration(
        privileges: [
            new CPrivilege(
                id: '1',
                type: 'method',
                name: 'priv',
                properties: [
                    'method': 'read',
                    'permission': '/some/path/'
                ]
            )
        ],
        roles: [
            new CRole(
                id: '0',
                name: 'name',
            ),
            new CRole(
                id: '',
                privileges: ['1']
            ),
            new CRole(
                id: 'arole',
                name: 'name',
                privileges: ['1']
            ),
            new CRole(
                id: 'recursive',
                name: 'recursive',
                roles: ['recursive']
            ),
            new CRole(
                id: 'recursive2',
                name: 'recursive2',
                roles: ['recursive3']
            ),
            new CRole(
                id: 'recursive3',
                name: 'recursive3',
                roles: ['recursive2']
            ),
            new CRole(
                id: 'recursive4',
                name: 'recursive4',
                roles: ['recursive5']
            ),
            new CRole(
                id: 'recursive5',
                name: 'recursive5',
                roles: ['recursive6']
            ),
            new CRole(
                id: 'recursive6',
                name: 'recursive6',
                roles: ['recursive4']
            ),
            new CRole(
                id: 'errantRole1',
                name: 'role',
                roles: ['errantRole2']
            ),
            new CRole(
                id: 'errantRole2',
                name: 'role',
                roles: ['errantRole3']
            ),
            new CRole(
                id: 'errantRole3',
                name: 'role',
                roles: ['errantRoleNotValid']
            )
        ]
    )
  }

  static Configuration securityModel3() {
    return new Configuration(
        privileges: [
            new CPrivilege(
                id: '',
                type: '',
                name: '',
                description: '',
                properties: [:]
            ),
            new CPrivilege(
                id: '25',
                type: 'method',
                name: 'priv',
                description: '',
                properties: [
                    'method': 'read',
                    'permission': '/some/path/'
                ]
            ),
            new CPrivilege(
                id: '5',
                type: 'method',
                name: 'priv',
                description: '',
                properties: [
                    'method': 'read',
                    'permission': '/some/path/'
                ]
            )
        ],
    )
  }

}

