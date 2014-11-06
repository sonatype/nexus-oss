package org.sonatype.nexus.yum.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
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
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:yumVersionedRepositories')
                ])
            ,
            new CPrivilege(
                id: 'yum-alias-read',
                type: 'method',
                name: 'Yum Alias - (read)',
                description: 'Give permission to read yum version aliases.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:yumAlias')
                ])
            ,
            new CPrivilege(
                id: 'yum-alias-create-read',
                type: 'method',
                name: 'Yum Alias - (update,read)',
                description: 'Give permission to create,update and read yum version aliases.',
                properties: [
                    new CProperty(key: 'method', value: 'create,update,read'),
                    new CProperty(key: 'permission', value: 'nexus:yumAlias')
                ])
        ],
        roles: [
            new CRole(
                id: 'nexus-yum-user',
                name: 'Nexus Yum Reader',
                description: 'Gives access to read versioned yum repositories',
                privileges: ['yum-repository-read']
            )
            ,
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

