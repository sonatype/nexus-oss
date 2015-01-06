/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.realms.ldap.internal.realms;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.security.realms.ldap.internal.LdapURL;
import org.sonatype.security.realms.ldap.internal.connector.DefaultLdapConnector;
import org.sonatype.security.realms.ldap.internal.connector.dao.LdapAuthConfiguration;
import org.sonatype.security.realms.ldap.internal.connector.dao.LdapDAOException;
import org.sonatype.security.realms.ldap.internal.connector.dao.LdapGroupDAO;
import org.sonatype.security.realms.ldap.internal.connector.dao.LdapUserDAO;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection;
import org.sonatype.security.realms.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.security.realms.ldap.internal.persist.entity.Mapping;

import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapConnectionUtils
{
  private static Logger logger = LoggerFactory.getLogger(LdapConnectionUtils.class);

  public static DefaultLdapContextFactory getLdapContextFactory(LdapConfiguration ldapServer,
                                                                boolean useBackupUrl)
      throws LdapDAOException
  {
    DefaultLdapContextFactory defaultLdapContextFactory = new DefaultLdapContextFactory();

    if (ldapServer == null) {
      throw new LdapDAOException("Ldap connection is not configured.");
    }

    Connection connInfo = ldapServer.getConnection();

    String url;
    try {
      if (useBackupUrl) {
        url = new LdapURL(connInfo.getBackupHost().getProtocol().name(), connInfo.getBackupHost().getHostName(),
            connInfo.getBackupHost().getPort(), connInfo.getSearchBase()).toString();
      }
      else {
        url = new LdapURL(connInfo.getHost().getProtocol().name(), connInfo.getHost().getHostName(),
            connInfo.getHost().getPort(), connInfo
            .getSearchBase()).toString();
      }
    }
    catch (MalformedURLException e) {
      // log an error, because the user could still log in and fix the config.
      logger.error("LDAP Configuration is Invalid.");
      throw new LdapDAOException("Invalid LDAP URL: " + e.getMessage());
    }

    defaultLdapContextFactory.setUsePooling(true);
    defaultLdapContextFactory.setUrl(url);
    defaultLdapContextFactory.setSystemUsername(connInfo.getSystemUsername());
    defaultLdapContextFactory.setSystemPassword(connInfo.getSystemPassword());
    defaultLdapContextFactory.setSearchBase(connInfo.getSearchBase());
    defaultLdapContextFactory.setAuthentication(connInfo.getAuthScheme());

    // get the timeout
    Map<String, String> connectionProperties = new HashMap<String, String>();
    connectionProperties.put("com.sun.jndi.ldap.connect.timeout",
        Integer.toString(ldapServer.getConnection().getConnectionTimeout() * 1000));

    // and the realm
    if (connInfo.getSaslRealm() != null) {
      connectionProperties.put("java.naming.security.sasl.realm", connInfo.getSaslRealm());
    }
    defaultLdapContextFactory.setAdditionalEnvironment(connectionProperties);

    return defaultLdapContextFactory;
  }

  public static LdapAuthConfiguration getLdapAuthConfiguration(LdapConfiguration ldapServer) {
    Mapping userAndGroupsConf = ldapServer.getMapping();
    LdapAuthConfiguration authConfig = new LdapAuthConfiguration();

    authConfig.setEmailAddressAttribute(userAndGroupsConf.getEmailAddressAttribute());
    authConfig.setUserBaseDn(StringUtils.defaultString(userAndGroupsConf.getUserBaseDn(), ""));
    authConfig.setUserIdAttribute(userAndGroupsConf.getUserIdAttribute());
    authConfig.setUserObjectClass(userAndGroupsConf.getUserObjectClass());
    authConfig.setPasswordAttribute(userAndGroupsConf.getUserPasswordAttribute());
    authConfig.setUserRealNameAttribute(userAndGroupsConf.getUserRealNameAttribute());

    authConfig.setGroupBaseDn(StringUtils.defaultString(userAndGroupsConf.getGroupBaseDn(), ""));
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

  public static void testUserAuthentication(LdapConfiguration ldapServer, LdapUserDAO ldapUserDAO,
                                            LdapGroupDAO ldapGroupDAO) throws LdapDAOException
  {
    LdapContextFactory contextFactory = getLdapContextFactory(ldapServer, false);
    LdapAuthConfiguration authConfig = getLdapAuthConfiguration(ldapServer);

    new DefaultLdapConnector("test", ldapUserDAO, ldapGroupDAO, contextFactory, authConfig);

  }

}
