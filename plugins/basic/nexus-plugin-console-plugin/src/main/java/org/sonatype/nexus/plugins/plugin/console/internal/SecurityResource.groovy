package org.sonatype.nexus.plugins.plugin.console.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Plugin console plugin static security resource.
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
                id: 'plugin-infos-read',
                type: 'method',
                name: 'Plugin Infos: Read',
                description: 'Give permission to read plugins\' information.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:pluginconsoleplugininfos')
                ])
        ],
        roles: [
            new CRole(
                id: 'ui-plugin-console',
                name: 'UI: Plugin Console',
                description: 'Gives access to the Plugin Console screen in Nexus UI.',
                privileges: ['plugin-infos-read']
            )
        ]
    )
  }
}

