package org.sonatype.nexus.unpack.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Unpack plugin static security resource.
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
                id: 'content-compressed',
                type: 'method',
                name: 'Unpack',
                description: 'Gives access to deploy compressed bundles onto Nexus',
                properties: [
                    new CProperty(key: 'method', value: 'create,update,delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:contentcompressed')
                ])
        ],
        roles: [
            new CRole(
                id: 'unpack',
                name: 'Unpack',
                description: 'Gives access to deploy compressed bundles onto Nexus',
                privileges: ['content-compressed']
            )
        ]
    )
  }
}

