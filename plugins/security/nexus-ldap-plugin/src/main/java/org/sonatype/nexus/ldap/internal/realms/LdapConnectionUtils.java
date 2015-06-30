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
package org.sonatype.nexus.ldap.internal.realms;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import com.sonatype.nexus.ssl.plugin.TrustStore;

import org.sonatype.nexus.ldap.internal.LdapURL;
import org.sonatype.nexus.ldap.internal.connector.dao.LdapAuthConfiguration;
import org.sonatype.nexus.ldap.internal.connector.dao.LdapDAOException;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection.Host;
import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.nexus.ldap.internal.persist.entity.Mapping;
import org.sonatype.nexus.ldap.internal.ssl.SSLLdapContextFactory;

import com.google.common.base.Strings;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapConnectionUtils
{
  private static Logger log = LoggerFactory.getLogger(LdapConnectionUtils.class);

  public static LdapContextFactory getLdapContextFactory(final LdapConfiguration configuration,
                                                         final TrustStore trustStore)
      throws LdapDAOException
  {
    if (configuration == null) {
      throw new LdapDAOException("Ldap connection is not configured.");
    }

    DefaultLdapContextFactory defaultLdapContextFactory = new DefaultLdapContextFactory();

    Connection connInfo = configuration.getConnection();
    Host host = connInfo.getHost();

    String url;
    try {
      url = new LdapURL(
          host.getProtocol().name(),
          host.getHostName(),
          host.getPort(),
          connInfo.getSearchBase()
      ).toString();
    }
    catch (MalformedURLException e) {
      // log an error, because the user could still log in and fix the config.
      log.error("LDAP Configuration is Invalid.");
      throw new LdapDAOException("Invalid LDAP URL: " + e.getMessage());
    }

    defaultLdapContextFactory.setUsePooling(true);
    defaultLdapContextFactory.setUrl(url);
    defaultLdapContextFactory.setSystemUsername(connInfo.getSystemUsername());
    defaultLdapContextFactory.setSystemPassword(connInfo.getSystemPassword());
    defaultLdapContextFactory.setSearchBase(connInfo.getSearchBase());
    defaultLdapContextFactory.setAuthentication(connInfo.getAuthScheme());

    // get the timeout
    Map<String, String> connectionProperties = new HashMap<>();
    connectionProperties.put("com.sun.jndi.ldap.connect.timeout",
        Integer.toString(connInfo.getConnectionTimeout() * 1000));

    // and the realm
    if (connInfo.getSaslRealm() != null) {
      connectionProperties.put("java.naming.security.sasl.realm", connInfo.getSaslRealm());
    }
    defaultLdapContextFactory.setAdditionalEnvironment(connectionProperties);

    if (host.getProtocol() == Connection.Protocol.ldaps && connInfo.getUseTrustStore()) {
      SSLContext sslContext = trustStore.getSSLContext();
      log.debug("Using Nexus SSL Trust Store for accessing {}:{}", host.getHostName(), host.getPort());
      return new SSLLdapContextFactory(sslContext, defaultLdapContextFactory);
    }
    log.debug("Using JVM Trust Store for accessing {}:{}", host.getHostName(), host.getPort());
    return defaultLdapContextFactory;
  }

  public static LdapAuthConfiguration getLdapAuthConfiguration(LdapConfiguration ldapServer) {
    Mapping userAndGroupsConf = ldapServer.getMapping();
    LdapAuthConfiguration authConfig = new LdapAuthConfiguration();

    authConfig.setEmailAddressAttribute(userAndGroupsConf.getEmailAddressAttribute());
    authConfig.setUserBaseDn(Strings.nullToEmpty(userAndGroupsConf.getUserBaseDn()));
    authConfig.setUserIdAttribute(userAndGroupsConf.getUserIdAttribute());
    authConfig.setUserObjectClass(userAndGroupsConf.getUserObjectClass());
    authConfig.setPasswordAttribute(userAndGroupsConf.getUserPasswordAttribute());
    authConfig.setUserRealNameAttribute(userAndGroupsConf.getUserRealNameAttribute());

    authConfig.setGroupBaseDn(Strings.nullToEmpty(userAndGroupsConf.getGroupBaseDn()));
    authConfig.setGroupIdAttribute(userAndGroupsConf.getGroupIdAttribute());
    authConfig.setGroupMemberAttribute(userAndGroupsConf.getGroupMemberAttribute());
    authConfig.setGroupMemberFormat(userAndGroupsConf.getGroupMemberFormat());
    authConfig.setGroupObjectClass(userAndGroupsConf.getGroupObjectClass());
    authConfig.setUserSubtree(userAndGroupsConf.isUserSubtree());
    authConfig.setGroupSubtree(userAndGroupsConf.isGroupSubtree());
    authConfig.setUserMemberOfAttribute(userAndGroupsConf.getUserMemberOfAttribute());
    authConfig.setLdapGroupsAsRoles(userAndGroupsConf.isLdapGroupsAsRoles());
    authConfig.setLdapFilter(userAndGroupsConf.getLdapFilter());
    return authConfig;
  }

}
