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
package org.sonatype.nexus.ldap.internal

import org.sonatype.nexus.security.config.CPrivilege
import org.sonatype.nexus.security.config.CRole
import org.sonatype.nexus.security.config.MemorySecurityConfiguration
import org.sonatype.nexus.security.config.StaticSecurityConfigurationResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * LDAP plugin static security resource.
 *
 * @since 3.0
 */
@Named
@Singleton
class SecurityConfigurationResource
implements StaticSecurityConfigurationResource
{
  @Override
  MemorySecurityConfiguration getConfiguration() {
    return new MemorySecurityConfiguration(
        privileges: [
            new CPrivilege(
                id: 'enterprise-ldap-create',
                type: 'method',
                name: 'LDAP (create,read)',
                description: 'Give permission to create new LDAP Servers.',
                properties: [
                    'method': 'create',
                    'permission': 'security:ldapconfig'
                ]
            ),
            new CPrivilege(
                id: 'enterprise-ldap-read',
                type: 'method',
                name: 'LDAP (read)',
                description: 'Give permission to read LDAP Server configurations.',
                properties: [
                    'method': 'read',
                    'permission': 'security:ldapconfig'
                ]
            ),
            new CPrivilege(
                id: 'enterprise-ldap-update',
                type: 'method',
                name: 'LDAP (update,read)',
                description: 'Give permission to update LDAP Server configurations.',
                properties: [
                    'method': 'update',
                    'permission': 'security:ldapconfig'
                ]
            ),
            new CPrivilege(
                id: 'enterprise-ldap-delete',
                type: 'method',
                name: 'LDAP (delete,read)',
                description: 'Give permission to delete LDAP Servers.',
                properties: [
                    'method': 'delete,read',
                    'permission': 'security:ldapconfig'
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

