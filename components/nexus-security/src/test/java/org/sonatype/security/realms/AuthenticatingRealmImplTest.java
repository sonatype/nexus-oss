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

package org.sonatype.security.realms;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.realms.tools.DefaultConfigurationManager;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.realm.Realm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AuthenticatingRealmImplTest
    extends AbstractSecurityTestCase
{
  private AuthenticatingRealmImpl realm;

  private DefaultConfigurationManager configurationManager;

  private PasswordService passwordService;

  private CUser testUser;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    realm = (AuthenticatingRealmImpl) lookup(Realm.class, "NexusAuthenticatingRealm");
    configurationManager = lookup(DefaultConfigurationManager.class);
    passwordService = lookup(PasswordService.class, "default");
  }

  public void testSuccessfulAuthentication() throws Exception {
    buildTestAuthenticationConfig(CUser.STATUS_ACTIVE);

    UsernamePasswordToken upToken = new UsernamePasswordToken("username", "password");
    AuthenticationInfo ai = realm.getAuthenticationInfo(upToken);
    String password = new String((char[]) ai.getCredentials());
    assertThat(this.passwordService.passwordsMatch("password", password), is(true));
  }

  public void testCreateWithPassowrd() throws Exception {
    buildTestAuthenticationConfig(CUser.STATUS_ACTIVE);

    String clearPassword = "default-password";
    String username = "testCreateWithPassowrdEmailUserId";

    CUser user = new CUser();
    user.setEmail("testCreateWithPassowrdEmail@somewhere");
    user.setFirstName("testCreateWithPassowrdEmail");
    user.setLastName("testCreateWithPassowrdEmail");
    user.setStatus(CUser.STATUS_ACTIVE);
    user.setId(username);

    Set<String> roles = new HashSet<String>();
    roles.add("role");

    configurationManager.createUser(user, clearPassword, roles);

    UsernamePasswordToken upToken = new UsernamePasswordToken("testCreateWithPassowrdEmailUserId", clearPassword);
    AuthenticationInfo ai = realm.getAuthenticationInfo(upToken);
    String password = new String((char[]) ai.getCredentials());

    assertThat(passwordService.passwordsMatch(clearPassword, password), is(true));
  }

  public void testFailedAuthentication() throws Exception {
    buildTestAuthenticationConfig(CUser.STATUS_ACTIVE);

    UsernamePasswordToken upToken = new UsernamePasswordToken("username", "badpassword");

    try {
      realm.getAuthenticationInfo(upToken);

      fail("Authentication should have failed");
    }
    catch (AuthenticationException e) {
      // good
    }
  }

  public void testDisabledAuthentication() throws Exception {
    buildTestAuthenticationConfig(CUser.STATUS_DISABLED);
    UsernamePasswordToken upToken = new UsernamePasswordToken("username", "password");

    try {
      realm.getAuthenticationInfo(upToken);

      fail("Authentication should have failed");
    }
    catch (AuthenticationException e) {
      // good
    }
  }

  public void testDetectLegacyUser() throws Exception {
    String password = "password";
    String username = "username";
    buildLegacyTestAuthenticationConfig(password);

    UsernamePasswordToken upToken = new UsernamePasswordToken(username, password);
    AuthenticationInfo ai = realm.getAuthenticationInfo(upToken);
    CUser updatedUser = this.configurationManager.readUser(username);
    String hash = new String((char[]) ai.getCredentials());

    assertThat(passwordService.passwordsMatch(password, hash), is(true));
    assertThat(passwordService.passwordsMatch(password, updatedUser.getPassword()), is(true));
  }

  private void buildTestAuthenticationConfig(String status) throws InvalidConfigurationException {
    buildTestAuthenticationConfig(status, this.hashPassword("password"));
  }

  private void buildTestAuthenticationConfig(String status, String hash) throws InvalidConfigurationException {
    CPrivilege priv = new CPrivilege();
    priv.setId("priv");
    priv.setName("name");
    priv.setDescription("desc");
    priv.setType("method");
    priv.setProperty("method", "read");
    priv.setProperty("permission", "somevalue");

    configurationManager.createPrivilege(priv);

    CRole role = new CRole();
    role.setName("name");
    role.setId("role");
    role.setDescription("desc");
    role.addPrivilege("priv");

    configurationManager.createRole(role);

    testUser = new CUser();
    testUser.setEmail("dummyemail@somewhere");
    testUser.setFirstName("dummyFirstName");
    testUser.setLastName("dummyLastName");
    testUser.setStatus(status);
    testUser.setId("username");
    testUser.setPassword(hash);

    Set<String> roles = new HashSet<String>();
    roles.add("role");

    configurationManager.createUser(testUser, roles);
  }

  private String hashPassword(String password) {
    return passwordService.encryptPassword(password);
  }

  private String legacyHashPassword(String password) {
    return Hashing.sha1().hashString(password, Charsets.UTF_8).toString();
  }

  private void buildLegacyTestAuthenticationConfig(String password) throws InvalidConfigurationException {
    buildTestAuthenticationConfig(CUser.STATUS_ACTIVE, legacyHashPassword(password));
  }
}
