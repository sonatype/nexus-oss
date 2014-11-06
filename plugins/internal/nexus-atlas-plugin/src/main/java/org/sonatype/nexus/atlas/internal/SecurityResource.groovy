package org.sonatype.nexus.atlas.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Atlas plugin static security resource.
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
                id: 'atlas-all',
                type: 'method',
                name: 'Atlas: Support Tools',
                description: 'Give permission to use Atlas support tools',
                properties: [
                    new CProperty(key: 'method', value: '*'),
                    new CProperty(key: 'permission', value: 'nexus:atlas')
                ])
        ],
        roles: [
            new CRole(
                id: 'atlas',
                name: 'Atlas: Support Tools',
                description: 'Gives access to Atlas support tools',
                privileges: ['atlas-all']
            )
        ]
    )
  }
}

