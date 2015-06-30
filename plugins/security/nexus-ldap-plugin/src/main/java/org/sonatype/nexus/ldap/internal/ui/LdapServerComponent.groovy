/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ldap.internal.ui

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.Validator
import javax.validation.constraints.NotNull
import javax.validation.groups.Default

import com.sonatype.nexus.ssl.plugin.TrustStore

import org.sonatype.nexus.common.text.Strings2
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.ldap.internal.LdapURL
import org.sonatype.nexus.ldap.internal.connector.dao.LdapConnectionTester
import org.sonatype.nexus.ldap.internal.connector.dao.LdapUser
import org.sonatype.nexus.ldap.internal.persist.LdapConfigurationManager
import org.sonatype.nexus.ldap.internal.persist.entity.Connection
import org.sonatype.nexus.ldap.internal.persist.entity.Connection.Host
import org.sonatype.nexus.ldap.internal.persist.entity.Connection.Protocol
import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration
import org.sonatype.nexus.ldap.internal.persist.entity.Mapping
import org.sonatype.nexus.ldap.internal.realms.EnterpriseLdapManager
import org.sonatype.nexus.ldap.internal.realms.LdapConnectionUtils
import org.sonatype.nexus.ldap.internal.templates.LdapSchemaTemplate
import org.sonatype.nexus.ldap.internal.templates.LdapSchemaTemplateManager
import org.sonatype.nexus.rapture.PasswordPlaceholder
import org.sonatype.nexus.validation.Validate
import org.sonatype.nexus.validation.group.Create
import org.sonatype.nexus.validation.group.Update

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import groovy.transform.PackageScope
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.apache.shiro.realm.ldap.LdapContextFactory
import org.hibernate.validator.constraints.NotEmpty

/**
 * LDAP Server {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'ldap_LdapServer')
class LdapServerComponent
    extends DirectComponentSupport
{

  @Inject
  LdapConfigurationManager ldapConfigurationManager

  @Inject
  LdapSchemaTemplateManager templateManager

  @Inject
  LdapConnectionTester ldapConnectionTester

  @Inject
  EnterpriseLdapManager ldapManager

  @Inject
  TrustStore trustStore

  @Inject
  Validator validator

  @DirectMethod
  @RequiresPermissions('nexus:ldap:read')
  List<LdapServerXO> read() {
    def counter = 1
    return ldapConfigurationManager.listLdapServerConfigurations().collect { input ->
      asLdapServerXO(input).with {
        order = counter++
        return it
      }
    }
  }

  @DirectMethod
  @RequiresPermissions('nexus:ldap:read')
  List<ReferenceXO> readReferences() {
    return ldapConfigurationManager.listLdapServerConfigurations().collect { input ->
      new ReferenceXO(
          id: input.id,
          name: input.name
      )
    }
  }

  @DirectMethod
  @RequiresPermissions('nexus:ldap:read')
  List<LdapSchemaTemplateXO> readTemplates() {
    return templateManager.schemaTemplates.collect { template ->
      asLdapSchemaTemplateXO(template)
    }
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:ldap:create')
  @Validate(groups = [Create, Default])
  LdapServerXO create(final @NotNull @Valid LdapServerXO ldapServerXO) {
    def id = ldapConfigurationManager.addLdapServerConfiguration(
        asCLdapServerConfiguration(validate(ldapServerXO), null)
    )
    return asLdapServerXO(ldapConfigurationManager.getLdapServerConfiguration(id))
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:ldap:update')
  @Validate(groups = [Update, Default])
  LdapServerXO update(final @NotNull @Valid LdapServerXO ldapServerXO) {
    LdapConfiguration existing = ldapConfigurationManager.getLdapServerConfiguration(ldapServerXO.id)
    if (existing) {
      ldapConfigurationManager.updateLdapServerConfiguration(
          asCLdapServerConfiguration(validate(ldapServerXO), existing.connection.systemPassword)
      )
      return asLdapServerXO(ldapConfigurationManager.getLdapServerConfiguration(ldapServerXO.id))
    }
    throw new IllegalArgumentException('LDAP server with id "' + ldapServerXO.id + '" not found')
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:ldap:delete')
  @Validate
  void remove(final @NotEmpty String id) {
    ldapConfigurationManager.deleteLdapServerConfiguration(id)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:ldap:update')
  void changeOrder(final List<String> orderedServerIds) {
    ldapConfigurationManager.setServerOrder(orderedServerIds)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:ldap:delete')
  void clearCache() {
    ldapConfigurationManager.clearCache()
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:ldap:update')
  @Validate
  void verifyConnection(final @NotNull @Valid LdapServerConnectionXO ldapServerConnectionXO) {
    String authPassword = null
    if (ldapServerConnectionXO.id) {
      LdapConfiguration existing = ldapConfigurationManager.getLdapServerConfiguration(ldapServerConnectionXO.id)
      if (!existing) {
        throw new IllegalArgumentException('LDAP server with id "' + ldapServerConnectionXO.id + '" not found')
      }
      authPassword = existing.connection.systemPassword
    }
    try {
      ldapConnectionTester.testConnection(buildLdapContextFactory(validate(ldapServerConnectionXO), authPassword))
    }
    catch (Exception e) {
      throw new Exception(buildReason('Failed to connect to LDAP Server', e))
    }
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:ldap:update')
  @Validate
  Collection<LdapUser> verifyUserMapping(final @NotNull @Valid LdapServerXO ldapServerXO) {
    String authPassword = null
    if (ldapServerXO.id) {
      LdapConfiguration existing = ldapConfigurationManager.getLdapServerConfiguration(ldapServerXO.id)
      if (!existing) {
        throw new IllegalArgumentException('LDAP server with id "' + ldapServerXO.id + '" not found')
      }
      authPassword = existing.connection.systemPassword
    }
    try {
      return ldapConnectionTester.testUserAndGroupMapping(
          buildLdapContextFactory(validate(ldapServerXO), authPassword),
          LdapConnectionUtils.getLdapAuthConfiguration(asCLdapServerConfiguration(ldapServerXO, authPassword)),
          20
      )
    }
    catch (Exception e) {
      throw new Exception(buildReason('Failed to connect to LDAP Server', e))
    }
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:ldap:update')
  @Validate
  void verifyLogin(final @NotNull @Valid LdapServerXO ldapServerXO,
                   final @NotEmpty String base64Username,
                   final @NotEmpty String base64Password)
  {
    String authPassword = null
    if (ldapServerXO.id) {
      LdapConfiguration existing = ldapConfigurationManager.getLdapServerConfiguration(ldapServerXO.id)
      if (!existing) {
        throw new IllegalArgumentException('LDAP server with id "' + ldapServerXO.id + '" not found')
      }
      authPassword = existing.connection.systemPassword
    }
    try {
      ldapManager.authenticateUserTest(
          Strings2.decodeBase64(base64Username),
          Strings2.decodeBase64(base64Password),
          asCLdapServerConfiguration(validate(ldapServerXO), authPassword)
      )
    }
    catch (Exception e) {
      throw new Exception(buildReason('Failed to connect to LDAP Server', e))
    }
  }

  @PackageScope
  LdapServerXO asLdapServerXO(final LdapConfiguration ldapServer) {
    Connection connectionInfo = ldapServer.connection
    Mapping userAndGroupConfig = ldapServer.mapping
    return new LdapServerXO(
        id: ldapServer.id,
        name: ldapServer.name,
        url: asLdapServerUrl(connectionInfo),
        protocol: connectionInfo.host.protocol.name(),
        useTrustStore: connectionInfo.useTrustStore,
        host: connectionInfo.host.hostName,
        port: connectionInfo.host.port,
        searchBase: connectionInfo.searchBase,

        authScheme: connectionInfo.authScheme,
        authRealm: connectionInfo.saslRealm,
        authUsername: connectionInfo.systemUsername,
        authPassword: PasswordPlaceholder.get(connectionInfo.systemPassword),

        connectionTimeout: connectionInfo.connectionTimeout,
        connectionRetryDelay: connectionInfo.connectionRetryDelay,
        maxIncidentsCount: connectionInfo.maxIncidentsCount,

        userBaseDn: userAndGroupConfig.userBaseDn,
        userSubtree: userAndGroupConfig.userSubtree,
        userObjectClass: userAndGroupConfig.userObjectClass,
        userLdapFilter: userAndGroupConfig.ldapFilter,
        userIdAttribute: userAndGroupConfig.userIdAttribute,
        userRealNameAttribute: userAndGroupConfig.userRealNameAttribute,
        userEmailAddressAttribute: userAndGroupConfig.emailAddressAttribute,
        userPasswordAttribute: userAndGroupConfig.userPasswordAttribute,

        ldapGroupsAsRoles: userAndGroupConfig.ldapGroupsAsRoles,
        groupType: userAndGroupConfig.ldapGroupsAsRoles ? (userAndGroupConfig.userMemberOfAttribute ? 'dynamic' : 'static') : null,
        userMemberOfAttribute: userAndGroupConfig.userMemberOfAttribute,
        groupBaseDn: userAndGroupConfig.groupBaseDn,
        groupSubtree: userAndGroupConfig.groupSubtree,
        groupIdAttribute: userAndGroupConfig.groupIdAttribute,
        groupMemberAttribute: userAndGroupConfig.groupMemberAttribute,
        groupMemberFormat: userAndGroupConfig.groupMemberFormat,
        groupObjectClass: userAndGroupConfig.groupObjectClass
    )
  }

  @PackageScope
  String asLdapServerUrl(final Connection connectionInfo) {
    return new LdapURL(
        connectionInfo.host.protocol.name(),
        connectionInfo.host.hostName,
        connectionInfo.host.port,
        connectionInfo.searchBase
    ).toString()
  }

  @PackageScope
  LdapSchemaTemplateXO asLdapSchemaTemplateXO(final LdapSchemaTemplate template) {
    Mapping userAndGroupConfig = template.userAndGroupAuthConfig
    return new LdapSchemaTemplateXO(
        name: template.name,

        userBaseDn: userAndGroupConfig.userBaseDn,
        userSubtree: userAndGroupConfig.userSubtree,
        userObjectClass: userAndGroupConfig.userObjectClass,
        userLdapFilter: userAndGroupConfig.ldapFilter,
        userIdAttribute: userAndGroupConfig.userIdAttribute,
        userRealNameAttribute: userAndGroupConfig.userRealNameAttribute,
        userEmailAddressAttribute: userAndGroupConfig.emailAddressAttribute,
        userPasswordAttribute: userAndGroupConfig.userPasswordAttribute,

        ldapGroupsAsRoles: userAndGroupConfig.ldapGroupsAsRoles,
        groupType: userAndGroupConfig.ldapGroupsAsRoles ? (userAndGroupConfig.userMemberOfAttribute ? 'dynamic' : 'static') : null,
        userMemberOfAttribute: userAndGroupConfig.userMemberOfAttribute,
        groupBaseDn: userAndGroupConfig.groupBaseDn,
        groupSubtree: userAndGroupConfig.groupSubtree,
        groupIdAttribute: userAndGroupConfig.groupIdAttribute,
        groupMemberAttribute: userAndGroupConfig.groupMemberAttribute,
        groupMemberFormat: userAndGroupConfig.groupMemberFormat,
        groupObjectClass: userAndGroupConfig.groupObjectClass
    )
  }

  @PackageScope
  LdapConfiguration asCLdapServerConfiguration(final LdapServerXO ldapServerXO, final String authPassword) {
    return new LdapConfiguration(
        id: ldapServerXO.id,
        name: ldapServerXO.name,
        connection: asConnection(ldapServerXO, authPassword),
        mapping: new Mapping(
            userBaseDn: ldapServerXO.userBaseDn,
            userSubtree: ldapServerXO.userSubtree ?: false,
            userObjectClass: ldapServerXO.userObjectClass,
            ldapFilter: ldapServerXO.userLdapFilter,
            userIdAttribute: ldapServerXO.userIdAttribute,
            userRealNameAttribute: ldapServerXO.userRealNameAttribute,
            emailAddressAttribute: ldapServerXO.userEmailAddressAttribute,
            userPasswordAttribute: ldapServerXO.userPasswordAttribute,

            ldapGroupsAsRoles: ldapServerXO.ldapGroupsAsRoles,
            userMemberOfAttribute: ldapServerXO.userMemberOfAttribute,
            groupBaseDn: ldapServerXO.groupBaseDn,
            groupSubtree: ldapServerXO.groupSubtree ?: false,
            groupIdAttribute: ldapServerXO.groupIdAttribute,
            groupMemberAttribute: ldapServerXO.groupMemberAttribute,
            groupMemberFormat: ldapServerXO.groupMemberFormat,
            groupObjectClass: ldapServerXO.groupObjectClass
        )
    )
  }

  @PackageScope
  Connection asConnection(final LdapServerConnectionXO connectionXO, final String authPassword) {
    return new Connection(
        host: new Host(Protocol.valueOf(connectionXO.protocol.name()), connectionXO.host, connectionXO.port),
        useTrustStore: Boolean.TRUE.equals(connectionXO.useTrustStore),
        searchBase: connectionXO.searchBase,

        authScheme: connectionXO.authScheme,
        saslRealm: connectionXO.authRealm,
        systemUsername: connectionXO.authUsername,
        systemPassword: PasswordPlaceholder.is(connectionXO.authPassword) ? authPassword : connectionXO.authPassword,

        connectionTimeout: connectionXO.connectionTimeout,
        connectionRetryDelay: connectionXO.connectionRetryDelay,
        maxIncidentsCount: connectionXO.maxIncidentsCount,
    )
  }

  @PackageScope
  LdapServerConnectionXO validate(final LdapServerConnectionXO ldapServerConnectionXO) {
    if (ldapServerConnectionXO.authScheme != 'none') {
      validator.validate(ldapServerConnectionXO, LdapServerConnectionXO.AuthScheme)
    }
    return ldapServerConnectionXO
  }

  @PackageScope
  LdapServerXO validate(final LdapServerXO ldapServerXO) {
    validate(ldapServerXO as LdapServerConnectionXO)
    if (ldapServerXO.ldapGroupsAsRoles) {
      validator.validate(
          ldapServerXO,
          ldapServerXO.groupType == 'static' ? LdapServerXO.GroupStatic : LdapServerXO.GroupDynamic
      )
    }
    return ldapServerXO
  }

  private LdapContextFactory buildLdapContextFactory(final LdapServerConnectionXO connectionXO,
                                                     final String authPassword)
  {
    return LdapConnectionUtils.getLdapContextFactory(
        new LdapConfiguration(connection: asConnection(connectionXO, authPassword)),
        trustStore
    )
  }

  @PackageScope
  String buildReason(final String userMessage, Throwable t) {
    String message = "${userMessage}: ${t.message}"

    while (t != t.cause && t.cause) {
      t = t.cause
      message += " [Caused by ${t.getClass().name}: ${t.message}]"
    }
    return message
  }

}
