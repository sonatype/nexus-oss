package org.sonatype.nexus.plugin.lucene.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Lucene indexer plugin static security resource.
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
                id: '17',
                type: 'method',
                name: 'Search Repositories',
                description: 'Give permission to perform searches of repository content.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:index')
                ])
            ,
            new CPrivilege(
                id: '18',
                type: 'method',
                name: 'Reindex',
                description: 'Give permission to Reindex repository content.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:index')
                ])
            ,
            new CPrivilege(
                id: '19',
                type: 'method',
                name: 'Checksum Search',
                description: 'Give permission to perform checksum type searches of repository content.  The extents of this privilege are related to the allowed targets.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:identify')
                ])
        ],
        roles: [
            new CRole(
                id: 'ui-search',
                name: 'UI: Search',
                description: 'Gives access to the Search screen in Nexus UI',
                privileges: ['17', '19', '54']
            )
            ,
            new CRole(
                id: 'anonymous',
                name: '',
                description: '',
                roles: ['ui-search']
            )
        ]
    )
  }
}

