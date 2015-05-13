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
package org.sonatype.nexus.capability.internal

import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.security.config.CPrivilege
import org.sonatype.nexus.security.config.CRole
import org.sonatype.nexus.security.config.MemorySecurityConfiguration
import org.sonatype.nexus.security.config.StaticSecurityConfigurationResource

/**
 * Capabilities plugin static security resource.
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
                id: 'capabilities-create-read',
                type: 'method',
                properties: [
                    'method': 'create,read',
                    'permission': 'nexus:capabilities'
                ]
            ),
            new CPrivilege(
                id: 'capabilities-read',
                type: 'method',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:capabilities'
                ]
            ),
            new CPrivilege(
                id: 'capabilities-update-read',
                type: 'method',
                properties: [
                    'method': 'update,read',
                    'permission': 'nexus:capabilities'
                ]
            ),
            new CPrivilege(
                id: 'capabilities-delete-read',
                type: 'method',
                properties: [
                    'method': 'delete,read',
                    'permission': 'nexus:capabilities'
                ]
            ),
            new CPrivilege(
                id: 'capability-types-read',
                type: 'method',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:capabilityTypes'
                ]
            )
        ],
        roles: [
            new CRole(
                id: 'ui-capabilities-admin',
                privileges: ['repositories-read', 'repogroups-read', 'capabilities-create-read', 'capabilities-read', 'capabilities-update-read', 'capabilities-delete-read', 'capability-types-read']
            )
        ]
    )
  }
}

