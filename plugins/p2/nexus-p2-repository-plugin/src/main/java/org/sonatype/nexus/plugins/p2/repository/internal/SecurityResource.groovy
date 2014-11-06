package org.sonatype.nexus.plugins.p2.repository.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * P2 repository plugin static security resource.
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
                id: 'p2-create',
                type: 'target',
                name: 'All P2 Repositories - (create)',
                description: 'Give permission to create any content in any P2 repository.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'repositoryTargetId', value: 'p2'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ])
            ,
            new CPrivilege(
                id: 'p2-read',
                type: 'target',
                name: 'All P2 Repositories - (read)',
                description: 'Give permission to read any content in any P2 Repository.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'repositoryTargetId', value: 'p2'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ])
            ,
            new CPrivilege(
                id: 'p2-update',
                type: 'target',
                name: 'All P2 Repositories - (update)',
                description: 'Give permission to update any content in any P2 Repository.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'repositoryTargetId', value: 'p2'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ])
            ,
            new CPrivilege(
                id: 'p2-delete',
                type: 'target',
                name: 'All P2 Repositories - (delete)',
                description: 'Give permission to delete any content in any P2 Repository.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'repositoryTargetId', value: 'p2'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ])
        ],
        roles: [
            new CRole(
                id: 'p2-all-read',
                name: 'Repo: All P2 Repositories (Read)',
                description: 'Gives access to read ALL content of ALL P2 Repositories in Nexus.',
                privileges: ['p2-read'],
                roles: ['p2-all-view']
            )
            ,
            new CRole(
                id: 'p2-all-full',
                name: 'Repo: All P2 Repositories (Full Control)',
                description: 'Gives access to create/read/update/delete ALL content of ALL P2 Repositories in Nexus.',
                privileges: ['p2-create', 'p2-read', 'p2-update', 'p2-delete'],
                roles: ['p2-all-view']
            )
        ]
    )
  }
}

