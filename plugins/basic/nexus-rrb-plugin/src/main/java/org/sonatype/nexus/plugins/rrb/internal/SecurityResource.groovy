package org.sonatype.nexus.plugins.rrb.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Browse remote plugin static security resource.
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
                id: 'browse-remote-repo',
                type: 'method',
                name: 'Browse Remote Repository - (read)',
                description: 'Give permission to browse remote repositories',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:browseremote')
                ])
        ],
        roles: [
            new CRole(
                id: 'ui-repo-browser',
                name: '',
                description: '',
                privileges: ['browse-remote-repo']
            )
        ]
    )
  }
}

