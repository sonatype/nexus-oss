package org.sonatype.nexus.obr.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
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
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'repositoryTargetId', value: 'obr'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ])
            ,
            new CPrivilege(
                id: 'obr-read',
                type: 'target',
                name: 'All OBR Repositories - (read)',
                description: 'Give permission to read any content in any OBR.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'repositoryTargetId', value: 'obr'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ])
            ,
            new CPrivilege(
                id: 'obr-update',
                type: 'target',
                name: 'All OBR Repositories - (update)',
                description: 'Give permission to update any content in any OBR.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'repositoryTargetId', value: 'obr'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ])
            ,
            new CPrivilege(
                id: 'obr-delete',
                type: 'target',
                name: 'All OBR Repositories - (delete)',
                description: 'Give permission to delete any content in any OBR.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'repositoryTargetId', value: 'obr'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ])
        ],
        roles: [
            new CRole(
                id: 'obr-all-read',
                name: 'Repo: All OBR Repositories (Read)',
                description: 'Gives access to read ALL content of ALL OBR Repositories in Nexus.',
                privileges: ['obr-read'],
                roles: ['obr-all-view']
            )
            ,
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

