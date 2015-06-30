/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.security.config.CPrivilege
import org.sonatype.nexus.security.config.MemorySecurityConfiguration
import org.sonatype.nexus.security.config.StaticSecurityConfigurationResource

/**
 * LDAP plugin static security resource.
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
                id: 'ldap-all',
                description: 'All permissions for LDAP',
                type: 'application',
                properties: [
                    domain : 'ldap',
                    actions: '*'
                ]
            ),
            new CPrivilege(
                id: 'ldap-create',
                description: 'Create permission for LDAP',
                type: 'application',
                properties: [
                    domain : 'ldap',
                    actions: 'create'
                ]
            ),
            new CPrivilege(
                id: 'ldap-read',
                description: 'Read permission for LDAP',
                type: 'application',
                properties: [
                    domain : 'ldap',
                    actions: 'read'
                ]
            ),
            new CPrivilege(
                id: 'ldap-update',
                description: 'Update permission for LDAP',
                type: 'application',
                properties: [
                    domain : 'ldap',
                    actions: 'update'
                ]
            ),
            new CPrivilege(
                id: 'ldap-delete',
                description: 'Delete permission for LDAP',
                type: 'application',
                properties: [
                    domain : 'ldap',
                    actions: 'delete,read'
                ]
            )
        ]
    )
  }
}

