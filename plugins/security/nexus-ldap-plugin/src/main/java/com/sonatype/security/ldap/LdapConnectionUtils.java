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
package com.sonatype.security.ldap;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;
import com.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;

import org.sonatype.security.ldap.dao.LdapAuthConfiguration;
import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.dao.LdapGroupDAO;
import org.sonatype.security.ldap.dao.LdapUserDAO;
import org.sonatype.security.ldap.realms.DefaultLdapContextFactory;
import org.sonatype.security.ldap.realms.connector.DefaultLdapConnector;
import org.sonatype.security.ldap.realms.tools.LdapURL;

import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapConnectionUtils
{
  private static Logger logger = LoggerFactory.getLogger(LdapConnectionUtils.class);

  public static DefaultLdapContextFactory getLdapContextFactory(CLdapServerConfiguration ldapServer,
                                                                boolean useBackupUrl)
      throws LdapDAOException
  {
    DefaultLdapContextFactory defaultLdapContextFactory = new DefaultLdapContextFactory();

    if (ldapServer == null) {
      throw new LdapDAOException("Ldap connection is not configured.");
    }

    CConnectionInfo connInfo = ldapServer.getConnectionInfo();

    String url;
    try {
      if (useBackupUrl) {
        url = new LdapURL(connInfo.getBackupMirrorProtocol(), connInfo.getBackupMirrorHost(), connInfo
            .getBackupMirrorPort(), connInfo.getSearchBase()).toString();
      }
      else {
        url = new LdapURL(connInfo.getProtocol(), connInfo.getHost(), connInfo.getPort(), connInfo
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
        Integer.toString(ldapServer.getConnectionInfo().getConnectionTimeout() * 1000));

    // and the realm
    if (connInfo.getRealm() != null) {
      connectionProperties.put("java.naming.security.sasl.realm", connInfo.getRealm());
    }
    defaultLdapContextFactory.setAdditionalEnvironment(connectionProperties);

    return defaultLdapContextFactory;
  }

  public static LdapAuthConfiguration getLdapAuthConfiguration(CLdapServerConfiguration ldapServer) {
    CUserAndGroupAuthConfiguration userAndGroupsConf = ldapServer.getUserAndGroupConfig();
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

  public static void testUserAuthentication(CLdapServerConfiguration ldapServer, LdapUserDAO ldapUserDAO,
                                            LdapGroupDAO ldapGroupDAO) throws LdapDAOException
  {
    LdapContextFactory contextFactory = getLdapContextFactory(ldapServer, false);
    LdapAuthConfiguration authConfig = getLdapAuthConfiguration(ldapServer);

    new DefaultLdapConnector("test", ldapUserDAO, ldapGroupDAO, contextFactory, authConfig);

  }

}
