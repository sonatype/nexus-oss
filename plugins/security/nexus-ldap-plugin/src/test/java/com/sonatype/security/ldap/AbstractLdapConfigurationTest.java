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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import com.sonatype.security.ldap.persist.PasswordHelper;
import com.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import com.sonatype.security.ldap.realms.persist.model.CLdapConfiguration;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;
import com.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;
import com.sonatype.security.ldap.realms.persist.model.io.xpp3.LdapConfigurationXpp3Reader;
import com.sonatype.security.ldap.realms.persist.model.io.xpp3.LdapConfigurationXpp3Writer;

import com.thoughtworks.xstream.XStream;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;

public abstract class AbstractLdapConfigurationTest
    extends AbstractEnterpriseLdapTest
{
  private PasswordHelper passwordHelper;

  @Override
  public void setUp()
      throws Exception
  {
    super.setUp();
    this.passwordHelper = this.lookup(PasswordHelper.class);

    copyResource("/defaults/security-configuration.xml", getSecurityConfiguration());
    copyResource("/defaults/security.xml", getNexusSecurityConfiguration());
  }

  protected CUserAndGroupAuthConfiguration buildUserAndGroupAuthConfiguration() {
    CUserAndGroupAuthConfiguration userGroupConf = new CUserAndGroupAuthConfiguration();

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

  protected CConnectionInfo buildConnectionInfo() throws UnsupportedEncodingException {
    CConnectionInfo connInfo = new CConnectionInfo();

    connInfo.setAuthScheme("ldap");
    connInfo.setBackupMirrorHost("backupHost");
    connInfo.setBackupMirrorPort(11111);
    connInfo.setBackupMirrorProtocol("ldap");
    connInfo.setCacheTimeout(30);
    connInfo.setConnectionRetryDelay(300);
    connInfo.setConnectionTimeout(15);
    connInfo.setHost("localhost");
    connInfo.setPort(386);
    connInfo.setProtocol("ldap");
    connInfo.setRealm("");
    connInfo.setSearchBase("ou=searchbase");
    connInfo.setSystemPassword(encodeBase64("systemPassword"));
    connInfo.setSystemUsername(encodeBase64("systemUsername"));

    return connInfo;
  }

  protected void compareConfigurations(String expectedConfigurationAsString, String actualConfigurationAsString)
      throws Exception
  {
    Assert.assertEquals(expectedConfigurationAsString.replace("\r", ""),
        this.clearPasswords(actualConfigurationAsString).replace("\r", ""));
  }

  protected void compareConfiguration(CLdapServerConfiguration expected, CLdapServerConfiguration actual)
      throws Exception
  {
    XStream xstream = new XStream();
    String originalConfig = xstream.toXML(expected);
    String newConfig = xstream.toXML(actual);

    Assert.assertEquals(originalConfig, newConfig);

    // now check against the file
    String fileConfig = xstream.toXML(this.getLdapServerConfigFromFile(expected.getId(), true));
    Assert.assertEquals(originalConfig, fileConfig);
  }

  protected CLdapServerConfiguration getLdapServerConfigFromFile(String id, boolean convertPasswordsToClearText)
      throws Exception
  {
    CLdapConfiguration config = this.getConfigFromFile();

    for (CLdapServerConfiguration ldapServer : config.getServers()) {
      if (ldapServer.getId().equals(id)) {
        if (convertPasswordsToClearText) {
          this.convertPasswordsToClearText(ldapServer);
        }
        return ldapServer;
      }
    }

    return null;
  }

  protected CLdapConfiguration getConfigFromFile()
      throws IOException, XmlPullParserException
  {
    try (FileReader fr = new FileReader(new File(getConfHomeDir(), "ldap.xml"))) {
      LdapConfigurationXpp3Reader reader = new LdapConfigurationXpp3Reader();
      return reader.read(fr);
    }
  }

  private String clearPasswords(String ldapConfigAsString)
      throws Exception
  {
    LdapConfigurationXpp3Reader reader = new LdapConfigurationXpp3Reader();
    CLdapConfiguration ldapConfiguration = reader.read(new StringReader(ldapConfigAsString));
    // loop through and set the passwords to clear text
    for (CLdapServerConfiguration ldapServer : ldapConfiguration.getServers()) {
      this.convertPasswordsToClearText(ldapServer);
    }

    LdapConfigurationXpp3Writer writer = new LdapConfigurationXpp3Writer();
    StringWriter stringWriter = new StringWriter();
    writer.write(stringWriter, ldapConfiguration);

    return stringWriter.toString();
  }

  private void convertPasswordsToClearText(CLdapServerConfiguration ldapServer)
      throws Exception
  {
    if (ldapServer.getConnectionInfo() != null
        && StringUtils.isNotEmpty(ldapServer.getConnectionInfo().getSystemPassword())) {
      // the password must be encrypted
      Assert.assertTrue(this.passwordHelper.isEncoded(ldapServer.getConnectionInfo().getSystemPassword()));
      ldapServer.getConnectionInfo().setSystemPassword(
          this.passwordHelper.decrypt(ldapServer.getConnectionInfo().getSystemPassword()));
    }
  }

}
