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
package org.sonatype.nexus.yum.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Yum repository plugin static security resource.
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
                id: 'yum-repository-read',
                type: 'method',
                name: 'Yum Versioned Repositories - (read)',
                description: 'Give permission to read versioned repositories.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:yumVersionedRepositories'
                ]
            ),
            new CPrivilege(
                id: 'yum-alias-read',
                type: 'method',
                name: 'Yum Alias - (read)',
                description: 'Give permission to read yum version aliases.',
                properties: [
                    'method': 'read',
                    'permission': 'nexus:yumAlias'
                ]
            ),
            new CPrivilege(
                id: 'yum-alias-create-read',
                type: 'method',
                name: 'Yum Alias - (update,read)',
                description: 'Give permission to create,update and read yum version aliases.',
                properties: [
                    'method': 'create,update,read',
                    'permission': 'nexus:yumAlias'
                ]
            )
        ],
        roles: [
            new CRole(
                id: 'nexus-yum-user',
                name: 'Nexus Yum Reader',
                description: 'Gives access to read versioned yum repositories',
                privileges: ['yum-repository-read']
            ),
            new CRole(
                id: 'nexus-yum-admin',
                name: 'Nexus Yum Admin',
                description: 'Gives access to read versioned yum repositories and administrate version aliases',
                privileges: ['yum-repository-read', 'yum-alias-read', 'yum-alias-create-read']
            )
        ]
    )
  }
}

