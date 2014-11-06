package org.sonatype.nexus.wonderland.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Wonderland plugin static security resource.
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
                id: 'wonderland-all',
                type: 'method',
                name: 'Wonderland',
                description: 'Give permission use Wonderland',
                properties: [
                    new CProperty(key: 'method', value: '*'),
                    new CProperty(key: 'permission', value: 'nexus:wonderland')
                ])
        ],
        roles: [
            new CRole(
                id: 'wonderland',
                name: 'Wonderland',
                description: 'Gives access to Wonderland',
                privileges: ['wonderland-all']
            )
        ]
    )
  }
}

