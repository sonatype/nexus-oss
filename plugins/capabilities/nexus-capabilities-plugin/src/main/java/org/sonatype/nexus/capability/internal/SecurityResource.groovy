package org.sonatype.nexus.capability.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * Capabilities plugin static security resource.
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
                id: 'capabilities-create-read',
                type: 'method',
                name: 'Capabilities - (create,read)',
                description: 'Give permission to create,read capability configurations.',
                properties: [
                    new CProperty(key: 'method', value: 'create,read'),
                    new CProperty(key: 'permission', value: 'nexus:capabilities')
                ])
            ,
            new CPrivilege(
                id: 'capabilities-read',
                type: 'method',
                name: 'Capabilities - (read)',
                description: 'Give permission to read existing configured capabilities.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:capabilities')
                ])
            ,
            new CPrivilege(
                id: 'capabilities-update-read',
                type: 'method',
                name: 'Capabilities - (update,read)',
                description: 'Give permission to update,read existing configured capabilities.',
                properties: [
                    new CProperty(key: 'method', value: 'update,read'),
                    new CProperty(key: 'permission', value: 'nexus:capabilities')
                ])
            ,
            new CPrivilege(
                id: 'capabilities-delete-read',
                type: 'method',
                name: 'Capabilities - (delete,read)',
                description: 'Give permission to delete,read existing configured capabilities.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'nexus:capabilities')
                ])
            ,
            new CPrivilege(
                id: 'capability-types-read',
                type: 'method',
                name: 'Capability Types - (read)',
                description: 'Give permission to retrieve list of support capability types available in nexus.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'nexus:capabilityTypes')
                ])
        ],
        roles: [
            new CRole(
                id: 'ui-capabilities-admin',
                name: 'UI: Capabilities Administration',
                description: 'Gives access to Capabilities Administration screen in Nexus UI',
                privileges: ['6', '14', 'capabilities-create-read', 'capabilities-read', 'capabilities-update-read', 'capabilities-delete-read', 'capability-types-read']
            )
        ]
    )
  }
}

