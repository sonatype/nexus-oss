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
package org.sonatype.nexus.obr.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * OBR repository plugin static security resource.
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
                id: 'obr-create',
                type: 'target',
                name: 'All OBR Repositories - (create)',
                description: 'Give permission to create any content in any OBR.',
                properties: [
                    'method': 'create,read',
                    'repositoryTargetId': 'obr',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'obr-read',
                type: 'target',
                name: 'All OBR Repositories - (read)',
                description: 'Give permission to read any content in any OBR.',
                properties: [
                    'method': 'read',
                    'repositoryTargetId': 'obr',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'obr-update',
                type: 'target',
                name: 'All OBR Repositories - (update)',
                description: 'Give permission to update any content in any OBR.',
                properties: [
                    'method': 'update,read',
                    'repositoryTargetId': 'obr',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            ),
            new CPrivilege(
                id: 'obr-delete',
                type: 'target',
                name: 'All OBR Repositories - (delete)',
                description: 'Give permission to delete any content in any OBR.',
                properties: [
                    'method': 'delete,read',
                    'repositoryTargetId': 'obr',
                    'repositoryId': '',
                    'repositoryGroupId': ''
                ]
            )
        ],
        roles: [
            new CRole(
                id: 'obr-all-read',
                name: 'Repo: All OBR Repositories (Read)',
                description: 'Gives access to read ALL content of ALL OBR Repositories in Nexus.',
                privileges: ['obr-read'],
                roles: ['obr-all-view']
            ),
            new CRole(
                id: 'obr-all-full',
                name: 'Repo: All OBRs (Full Control)',
                description: 'Gives access to create/read/update/delete ALL content of ALL OBR Repositories in Nexus.',
                privileges: ['obr-create', 'obr-read', 'obr-update', 'obr-delete'],
                roles: ['obr-all-view']
            )
        ]
    )
  }
}

