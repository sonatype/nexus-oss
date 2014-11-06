package org.sonatype.nexus.timeline.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Timeline plugin static security resource.
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
                id: '44',
                type: 'method',
                name: 'Feeds - (read)',
                description: 'Give permission to view the different feeds. The extents of this privilege are related to the allowed targets.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:feeds')
                ])
        ],
        roles: [
            new CRole(
                id: 'ui-system-feeds',
                name: 'UI: System Feeds',
                description: 'Gives access to the System Feeds screen in Nexus UI',
                privileges: ['44']
            )
        ]
    )
  }
}

