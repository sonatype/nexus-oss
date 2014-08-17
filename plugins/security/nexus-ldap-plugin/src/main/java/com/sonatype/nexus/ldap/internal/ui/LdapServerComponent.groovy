/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.ldap.internal.ui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import com.sonatype.nexus.ldap.internal.ssl.SSLLdapContextFactory
import com.sonatype.nexus.ldap.model.LdapTrustStoreKey
import com.sonatype.nexus.ssl.plugin.TrustStore
import com.sonatype.security.ldap.EnterpriseLdapManager
import com.sonatype.security.ldap.LdapConnectionUtils
import com.sonatype.security.ldap.persist.LdapConfigurationManager
import com.sonatype.security.ldap.realms.persist.model.CConnectionInfo
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration
import com.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration
import com.sonatype.security.ldap.templates.LdapSchemaTemplate
import com.sonatype.security.ldap.templates.LdapSchemaTemplateManager
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.apache.shiro.realm.ldap.LdapContextFactory
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.nexus.rapture.TrustStoreKeys
import org.sonatype.nexus.util.Tokens
import org.sonatype.nexus.validation.Create
import org.sonatype.nexus.validation.Update
import org.sonatype.nexus.validation.Validate
import org.sonatype.security.ldap.dao.LdapConnectionTester
import org.sonatype.security.ldap.dao.LdapUser
import org.sonatype.security.ldap.realms.DefaultLdapContextFactory
import org.sonatype.security.ldap.realms.tools.LdapURL

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.validation.Valid
import javax.validation.Validator
import javax.validation.constraints.NotNull
import javax.validation.groups.Default

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
  @Nullable
  TrustStoreKeys trustStoreKeys

  @Inject
  Validator validator

  @DirectMethod
  @RequiresPermissions('security:ldapconfig:read')
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
  @RequiresPermissions('security:ldapconfig:read')
  List<ReferenceXO> readReferences() {
    return ldapConfigurationManager.listLdapServerConfigurations().collect { input ->
      new ReferenceXO(
          id: input.id,
          name: input.name
      )
    }
  }

  @DirectMethod
  @RequiresPermissions('security:ldapconfig:read')
  List<LdapSchemaTemplateXO> readTemplates() {
    return templateManager.schemaTemplates.collect { template ->
      asLdapSchemaTemplateXO(template)
    }
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:ldapconfig:create')
  @Validate(groups = [Create, Default])
  LdapServerXO create(final @NotNull(message = '[ldapServerXO] may not be null') @Valid LdapServerXO ldapServerXO) {
    ldapServerXO.id = Long.toHexString(System.nanoTime())
    ldapConfigurationManager.addLdapServerConfiguration(asCLdapServerConfiguration(validate(ldapServerXO), null))
    trustStoreKeys?.setEnabled(LdapTrustStoreKey.TYPE, ldapServerXO.id, ldapServerXO.useTrustStore)
    return asLdapServerXO(ldapConfigurationManager.getLdapServerConfiguration(ldapServerXO.id))
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:ldapconfig:update')
  @Validate(groups = [Update, Default])
  LdapServerXO update(final @NotNull(message = '[ldapServerXO] may not be null') @Valid LdapServerXO ldapServerXO) {
    CLdapServerConfiguration existing = ldapConfigurationManager.getLdapServerConfiguration(ldapServerXO.id)
    if (existing) {
      ldapConfigurationManager.updateLdapServerConfiguration(asCLdapServerConfiguration(validate(ldapServerXO), existing.connectionInfo.systemPassword))
      trustStoreKeys?.setEnabled(LdapTrustStoreKey.TYPE, ldapServerXO.id, ldapServerXO.useTrustStore)
      return asLdapServerXO(ldapConfigurationManager.getLdapServerConfiguration(ldapServerXO.id))
    }
    throw new IllegalArgumentException('LDAP server with id "' + ldapServerXO.id + '" not found')
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:ldapconfig:delete')
  @Validate
  void delete_(final @NotEmpty(message = '[id] may not be empty') String id) {
    ldapConfigurationManager.deleteLdapServerConfiguration(id)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:ldapconfig:update')
  void changeOrder(final List<String> orderedServerIds) {
    ldapConfigurationManager.setServerOrder(orderedServerIds)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:ldapconfig:delete')
  void clearCache() {
    ldapConfigurationManager.clearCache()
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:ldapconfig:update')
  @Validate
  void verifyConnection(final @NotNull(message = '[ldapServerXO] may not be null') @Valid LdapServerXO ldapServerXO) {
    String authPassword = null
    if (ldapServerXO.id) {
      CLdapServerConfiguration existing = ldapConfigurationManager.getLdapServerConfiguration(ldapServerXO.id)
      if (!existing) {
        throw new IllegalArgumentException('LDAP server with id "' + ldapServerXO.id + '" not found')
      }
      authPassword = existing.connectionInfo.systemPassword
    }
    try {
      ldapConnectionTester.testConnection(buildLdapContextFactory(ldapServerXO, authPassword))
    }
    catch (Exception e) {
      throw new Exception(buildReason('Failed to connect to LDAP Server', e))
    }
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('security:ldapconfig:update')
  @Validate
  Collection<LdapUser> verifyUserMapping(final @NotNull(message = '[ldapServerXO] may not be null') @Valid LdapServerXO ldapServerXO) {
    String authPassword = null
    if (ldapServerXO.id) {
      CLdapServerConfiguration existing = ldapConfigurationManager.getLdapServerConfiguration(ldapServerXO.id)
      if (!existing) {
        throw new IllegalArgumentException('LDAP server with id "' + ldapServerXO.id + '" not found')
      }
      authPassword = existing.connectionInfo.systemPassword
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
  @RequiresPermissions('security:ldapconfig:update')
  @Validate
  void verifyLogin(final @NotNull(message = '[ldapServerXO] may not be null') @Valid LdapServerXO ldapServerXO,
                   final @NotEmpty(message = '[base64Username] may not be empty') String base64Username,
                   final @NotEmpty(message = '[base64Password] may not be empty') String base64Password)
  {
    String authPassword = null
    if (ldapServerXO.id) {
      CLdapServerConfiguration existing = ldapConfigurationManager.getLdapServerConfiguration(ldapServerXO.id)
      if (!existing) {
        throw new IllegalArgumentException('LDAP server with id "' + ldapServerXO.id + '" not found')
      }
      authPassword = existing.connectionInfo.systemPassword
    }
    try {
      ldapManager.authenticateUserTest(
          Tokens.decodeBase64String(base64Username),
          Tokens.decodeBase64String(base64Password),
          asCLdapServerConfiguration(validate(ldapServerXO), authPassword)
      )
    }
    catch (Exception e) {
      throw new Exception(buildReason('Failed to connect to LDAP Server', e))
    }
  }

  LdapServerXO asLdapServerXO(final CLdapServerConfiguration ldapServer) {
    CConnectionInfo connectionInfo = ldapServer.connectionInfo
    CUserAndGroupAuthConfiguration userAndGroupConfig = ldapServer.userAndGroupConfig
    return new LdapServerXO(
        id: ldapServer.id,
        name: ldapServer.name,
        url: asLdapServerUrl(connectionInfo),
        protocol: connectionInfo.protocol,
        useTrustStore: trustStoreKeys?.isEnabled(LdapTrustStoreKey.TYPE, ldapServer.id),
        host: connectionInfo.host,
        port: connectionInfo.port,
        searchBase: connectionInfo.searchBase,

        authScheme: connectionInfo.authScheme,
        authRealm: connectionInfo.realm,
        authUsername: connectionInfo.systemUsername,
        authPassword: connectionInfo.systemPassword ? Password.fakePassword() : null,

        connectionTimeout: connectionInfo.connectionTimeout,
        connectionRetryDelay: connectionInfo.connectionRetryDelay,
        cacheTimeout: connectionInfo.cacheTimeout,

        backupMirrorEnabled: connectionInfo.backupMirrorHost,
        backupMirrorProtocol: connectionInfo.backupMirrorProtocol,
        backupMirrorHost: connectionInfo.backupMirrorHost,
        backupMirrorPort: connectionInfo.backupMirrorPort > 0 ? connectionInfo.backupMirrorPort : null,

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

  private static String asLdapServerUrl(final CConnectionInfo connectionInfo) {
    return new LdapURL(
        connectionInfo.getProtocol(),
        connectionInfo.getHost(),
        connectionInfo.getPort(),
        connectionInfo.getSearchBase()
    ).toString()
  }

  private static LdapSchemaTemplateXO asLdapSchemaTemplateXO(final LdapSchemaTemplate template) {
    CUserAndGroupAuthConfiguration userAndGroupConfig = template.userAndGroupAuthConfig
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

  private static CLdapServerConfiguration asCLdapServerConfiguration(final LdapServerXO ldapServerXO, final String authPassword) {
    return new CLdapServerConfiguration(
        id: ldapServerXO.id,
        name: ldapServerXO.name,
        connectionInfo: new CConnectionInfo(
            protocol: ldapServerXO.protocol,
            host: ldapServerXO.host,
            port: ldapServerXO.port ?: 0,
            searchBase: ldapServerXO.searchBase,

            authScheme: ldapServerXO.authScheme,
            realm: ldapServerXO.authRealm,
            systemUsername: ldapServerXO.authUsername,
            systemPassword: ldapServerXO.authPassword?.valueIfValid ?: authPassword,

            connectionTimeout: ldapServerXO.connectionTimeout,
            connectionRetryDelay: ldapServerXO.connectionRetryDelay,
            cacheTimeout: ldapServerXO.cacheTimeout,

            backupMirrorProtocol: ldapServerXO.backupMirrorEnabled ? ldapServerXO.backupMirrorProtocol : null,
            backupMirrorHost: ldapServerXO.backupMirrorEnabled ? ldapServerXO.backupMirrorHost : null,
            backupMirrorPort: ldapServerXO.backupMirrorEnabled ? ldapServerXO.backupMirrorPort : 0
        ),
        userAndGroupConfig: new CUserAndGroupAuthConfiguration(
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

  LdapServerXO validate(final LdapServerXO ldapServerXO) {
    if (ldapServerXO.authScheme != 'none') {
      validator.validate(ldapServerXO, LdapServerXO.AuthScheme)
    }
    if (ldapServerXO.backupMirrorEnabled) {
      validator.validate(ldapServerXO, LdapServerXO.BackupMirror)
    }
    if (ldapServerXO.ldapGroupsAsRoles) {
      validator.validate(ldapServerXO, ldapServerXO.groupType == 'static' ? LdapServerXO.GroupStatic : LdapServerXO.GroupDynamic)
    }
    return ldapServerXO
  }

  private LdapContextFactory buildLdapContextFactory(final LdapServerXO ldapServerXO, final String authPassword) {
    DefaultLdapContextFactory ldapContextFactory = new DefaultLdapContextFactory(
        authentication: ldapServerXO.authScheme,
        searchBase: ldapServerXO.searchBase,
        systemUsername: ldapServerXO.authUsername,
        systemPassword: ldapServerXO.authPassword?.valueIfValid ?: authPassword,
        url: new LdapURL(ldapServerXO.protocol.toString(), ldapServerXO.host, ldapServerXO.port, ldapServerXO.searchBase)
    )
    if (ldapServerXO.protocol == LdapServerXO.Protocol.ldaps && ldapServerXO.useTrustStore) {
      final SSLContext sslContext = trustStore.getSSLContext()
      log.debug "Using Nexus SSL Trust Store for accessing ${ldapServerXO.host}:${ldapServerXO.port}"
      return new SSLLdapContextFactory(sslContext, ldapContextFactory)
    }
    log.debug "Using JVM Trust Store for accessing ${ldapServerXO.host}:${ldapServerXO.port}"
    return ldapContextFactory
  }

  private static String buildReason(final String userMessage, Throwable t) {
    String message = "${userMessage}: ${t.message}"

    while (t != t.cause && t.cause) {
      t = t.cause
      message += " [Caused by ${t.getClass().name}: ${t.message}]"
    }
    return message
  }

}
