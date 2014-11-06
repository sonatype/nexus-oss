package com.sonatype.nexus.ldap.internal

import org.sonatype.security.model.CPrivilege
import org.sonatype.security.model.CProperty
import org.sonatype.security.model.CRole
import org.sonatype.security.model.Configuration
import org.sonatype.security.realms.tools.StaticSecurityResource

import javax.inject.Named
import javax.inject.Singleton

/**
 * LDAP plugin static security resource.
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
                id: 'enterprise-ldap-create',
                type: 'method',
                name: 'LDAP (create,read)',
                description: 'Give permission to create new LDAP Servers.',
                properties: [
                    new CProperty(key: 'method', value: 'create'),
                    new CProperty(key: 'permission', value: 'security:ldapconfig')
                ])
            ,
            new CPrivilege(
                id: 'enterprise-ldap-read',
                type: 'method',
                name: 'LDAP (read)',
                description: 'Give permission to read LDAP Server configurations.',
                properties: [
                    new CProperty(key: 'method', value: 'read'),
                    new CProperty(key: 'permission', value: 'security:ldapconfig')
                ])
            ,
            new CPrivilege(
                id: 'enterprise-ldap-update',
                type: 'method',
                name: 'LDAP (update,read)',
                description: 'Give permission to update LDAP Server configurations.',
                properties: [
                    new CProperty(key: 'method', value: 'update'),
                    new CProperty(key: 'permission', value: 'security:ldapconfig')
                ])
            ,
            new CPrivilege(
                id: 'enterprise-ldap-delete',
                type: 'method',
                name: 'LDAP (delete,read)',
                description: 'Give permission to delete LDAP Servers.',
                properties: [
                    new CProperty(key: 'method', value: 'delete,read'),
                    new CProperty(key: 'permission', value: 'security:ldapconfig')
                ])
        ],
        roles: [
            new CRole(
                id: 'ui-enterprise-ldap-admin',
                name: 'UI: LDAP Administrator',
                description: 'Gives access to create and edit LDAP Servers.',
                privileges: ['enterprise-ldap-create', 'enterprise-ldap-read', 'enterprise-ldap-update', 'enterprise-ldap-delete']
            )
        ]
    )
  }
}

