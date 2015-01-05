/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.realms.ldap.internal;

import java.io.UnsupportedEncodingException;

import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection.Host;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection.Protocol;
import org.sonatype.security.realms.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.security.realms.ldap.internal.persist.entity.Mapping;

import com.thoughtworks.xstream.XStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class AbstractLdapConfigurationTest
    extends LdapITSupport
{
  @Override
  protected SecurityConfiguration getSecurityConfig() {
    return SecurityTestSupportSecurity.securityWithLdapRealm();
  }

  protected Mapping buildUserAndGroupAuthConfiguration() {
    final Mapping userGroupConf = new Mapping();
    userGroupConf.setUserMemberOfAttribute("userMemberOfAttribute");
    userGroupConf.setGroupBaseDn("groupBaseDn");
    userGroupConf.setGroupIdAttribute("groupIdAttribute");
    userGroupConf.setGroupMemberAttribute("groupMemberAttribute");
    userGroupConf.setGroupMemberFormat("groupMemberFormat");
    userGroupConf.setGroupObjectClass("groupObjectClass");
    userGroupConf.setLdapGroupsAsRoles(true);
    userGroupConf.setEmailAddressAttribute("emailAddressAttribute");
    userGroupConf.setUserBaseDn("userBaseDn");
    userGroupConf.setUserIdAttribute("userIdAttribute");
    userGroupConf.setUserObjectClass("userObjectClass");
    userGroupConf.setUserPasswordAttribute("userPasswordAttribute");
    userGroupConf.setUserRealNameAttribute("userRealNameAttribute");
    userGroupConf.setUserSubtree(true);
    return userGroupConf;
  }

  protected Connection buildConnectionInfo() throws UnsupportedEncodingException {
    Connection connInfo = new Connection();
    connInfo.setAuthScheme("ldap");
    connInfo.setBackupHost(new Host(Protocol.ldap, "backupHost", 11111));
    connInfo.setCacheTimeout(30);
    connInfo.setConnectionRetryDelay(300);
    connInfo.setConnectionTimeout(15);
    connInfo.setHost(new Host(Protocol.ldap, "localhost", 386));
    connInfo.setSaslRealm("");
    connInfo.setSearchBase("ou=searchbase");
    connInfo.setSystemPassword(encodeBase64("systemPassword"));
    connInfo.setSystemUsername(encodeBase64("systemUsername"));
    return connInfo;
  }

  protected void compareConfiguration(LdapConfiguration expected, LdapConfiguration actual)
      throws Exception
  {
    XStream xstream = new XStream();
    String originalConfig = xstream.toXML(expected);
    String newConfig = xstream.toXML(actual);
    assertThat(newConfig, equalTo(originalConfig));
  }
}
