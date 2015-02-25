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
package org.sonatype.nexus.repository.site.plugin.internal

import org.sonatype.nexus.security.config.CPrivilege
import org.sonatype.nexus.security.config.CRole
import org.sonatype.nexus.security.config.MemorySecurityConfiguration
import org.sonatype.nexus.security.config.StaticSecurityConfigurationResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Site repository plugin static security resource.
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
                id: 'site-create',
                type: 'target',
                name: 'All Site Repositories - (create)',
                description: 'Give permission to create any content in any Site repository.',
                properties: [
                    'method': 'create,read',
                    'repositoryTargetId': 'site',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'site-read',
                type: 'target',
                name: 'All Site Repositories - (read)',
                description: 'Give permission to read any content in any Site Repository.',
                properties: [
                    'method': 'read',
                    'repositoryTargetId': 'site',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'site-update',
                type: 'target',
                name: 'All Site Repositories - (update)',
                description: 'Give permission to update any content in any Site Repository.',
                properties: [
                    'method': 'update,read',
                    'repositoryTargetId': 'site',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'site-delete',
                type: 'target',
                name: 'All Site Repositories - (delete)',
                description: 'Give permission to delete any content in any Site Repository.',
                properties: [
                    'method': 'delete,read',
                    'repositoryTargetId': 'site',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            )
        ],
        roles: [
            new CRole(
                id: 'site-all-read',
                name: 'Repo: All Site Repositories (Read)',
                description: 'Gives access to read ALL content of ALL Site Repositories in Nexus.',
                privileges: ['site-read', 'repository-all']
            ),
            new CRole(
                id: 'site-all-full',
                name: 'Repo: All Site Repositories (Full Control)',
                description: 'Gives access to create/read/update/delete ALL content of ALL Site Repositories in Nexus.',
                privileges: ['site-create', 'site-read', 'site-update', 'site-delete', 'repository-all']
            )
        ]
    )
  }
}

