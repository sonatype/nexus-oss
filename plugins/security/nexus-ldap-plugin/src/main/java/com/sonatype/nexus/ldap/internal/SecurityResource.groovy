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
package com.sonatype.nexus.ldap.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * LDAP plugin static security resource.
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
                id: 'enterprise-ldap-create',
                type: 'method',
                name: 'LDAP (create,read)',
                description: 'Give permission to create new LDAP Servers.',
                properties: [
                    new CProperty(key: 'method', value: 'create'),
                    new CProperty(key: 'permission', value: 'security:ldapconfig')
                ]
            ),
            new CPrivilege(
                id: 'enterprise-ldap-read',
                type: 'method',
                name: 'LDAP (read)',
                description: 'Give permission to read LDAP Server configurations.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'security:ldapconfig')
                ]
            ),
            new CPrivilege(
                id: 'enterprise-ldap-update',
                type: 'method',
                name: 'LDAP (update,read)',
                description: 'Give permission to update LDAP Server configurations.',
                properties: [
                    new CProperty(key: 'method', value: 'update'),
                    new CProperty(key: 'permission', value: 'security:ldapconfig')
                ]
            ),
            new CPrivilege(
                id: 'enterprise-ldap-delete',
                type: 'method',
                name: 'LDAP (delete,read)',
                description: 'Give permission to delete LDAP Servers.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'security:ldapconfig')
                ]
            )
        ],
        roles: [
            new CRole(
                id: 'ui-enterprise-ldap-admin',
                name: 'UI: LDAP Administrator',
                description: 'Gives access to create and edit LDAP Servers.',
                privileges: ['enterprise-ldap-create', 'enterprise-ldap-read', 'enterprise-ldap-update', 'enterprise-ldap-delete']
            )
        ]
    )
  }
}

