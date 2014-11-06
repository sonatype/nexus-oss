package org.sonatype.nexus.repository.site.plugin.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Site repository plugin static security resource.
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
                id: 'site-create',
                type: 'target',
                name: 'All Site Repositories - (create)',
                description: 'Give permission to create any content in any Site repository.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'repositoryTargetId', value: 'site'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ])
            ,
            new CPrivilege(
                id: 'site-read',
                type: 'target',
                name: 'All Site Repositories - (read)',
                description: 'Give permission to read any content in any Site Repository.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'repositoryTargetId', value: 'site'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ])
            ,
            new CPrivilege(
                id: 'site-update',
                type: 'target',
                name: 'All Site Repositories - (update)',
                description: 'Give permission to update any content in any Site Repository.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'repositoryTargetId', value: 'site'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ])
            ,
            new CPrivilege(
                id: 'site-delete',
                type: 'target',
                name: 'All Site Repositories - (delete)',
                description: 'Give permission to delete any content in any Site Repository.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'repositoryTargetId', value: 'site'),
                    new CProperty(key: 'repositoryId', value: ''),
                    new CProperty(key: 'repositoryGroupId', value: '')
                ])
        ],
        roles: [
            new CRole(
                id: 'site-all-read',
                name: 'Repo: All Site Repositories (Read)',
                description: 'Gives access to read ALL content of ALL Site Repositories in Nexus.',
                privileges: ['site-read', 'repository-all']
            )
            ,
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

