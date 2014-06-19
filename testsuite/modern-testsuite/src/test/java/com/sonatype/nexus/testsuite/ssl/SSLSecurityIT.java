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

package com.sonatype.nexus.testsuite.ssl;

import java.util.Collection;

import com.sonatype.nexus.ssl.client.Certificate;
import com.sonatype.nexus.ssl.client.TrustStore;

import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.core.subsystem.security.Role;
import org.sonatype.nexus.client.core.subsystem.security.Roles;
import org.sonatype.nexus.client.core.subsystem.security.User;
import org.sonatype.nexus.client.core.subsystem.security.Users;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;

/**
 * ITs related to trusted keys management.
 *
 * @since 1.0
 */
public class SSLSecurityIT
    extends SSLITSupport
{

  private static final String TEST_USER_PASSWORD = "xyz123";

  private NexusClient clientForTestUser;

  private Role role;

  public SSLSecurityIT(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  @Before
  public void prepare() {
    final String roleName = uniqueName("r");
    role = client().getSubsystem(Roles.class).create(roleName)
        .withName(roleName)
        .withRole("anonymous")
        .save();

    final User user = client().getSubsystem(Users.class).create(uniqueName("u"))
        .withEmail("foo_bar@sonatype.org")
        .withFirstName("bar")
        .withLastName("foo")
        .withPassword(TEST_USER_PASSWORD)
        .withRole(role.id())
        .save();

    clientForTestUser = createNexusClient(nexus(), user.id(), TEST_USER_PASSWORD);

    // remove all trusted keys
    final Collection<Certificate> trustedKeys = truststore().get();
    for (final Certificate trustedKey : trustedKeys) {
      trustedKey.remove();
    }
  }

  /**
   * Verify that trusted keys can be read only when user has "ssl-truststore-read" or "ssl-truststore-create" or
   * "ssl-truststore-delete" permission.
   */
  @Test
  public void read() {
    try {
      clientForTestUser.getSubsystem(TrustStore.class).get();
      assertThat("Expected to fail with 403 Forbidden", false);
    }
    catch (UniformInterfaceException e) {
      // expected as user does not have the necessary permissions
      assertThat(e.getResponse().getClientResponseStatus(), Matchers.is(Status.FORBIDDEN));
    }

    role.withPrivilege("ssl-truststore-read").save();
    clientForTestUser.getSubsystem(TrustStore.class).get();
    role.removePrivilege("ssl-truststore-read").save();

    role.withPrivilege("ssl-truststore-create").save();
    clientForTestUser.getSubsystem(TrustStore.class).get();
    role.removePrivilege("ssl-truststore-create").save();

    role.withPrivilege("ssl-truststore-delete").save();
    clientForTestUser.getSubsystem(TrustStore.class).get();
    role.removePrivilege("ssl-truststore-delete").save();
  }

  /**
   * Verify that trusted keys can be created only when user has "ssl-truststore-create" permission.
   */
  @Test
  public void create()
      throws Exception
  {
    try {
      clientForTestUser.getSubsystem(TrustStore.class).create()
          .withPem(FileUtils.readFileToString(testData().resolveFile("pem.txt")))
          .save();
      assertThat("Expected to fail with 403 Forbidden", false);
    }
    catch (UniformInterfaceException e) {
      // expected as user does not have the necessary permissions
      assertThat(e.getResponse().getClientResponseStatus(), Matchers.is(Status.FORBIDDEN));
    }

    role.withPrivilege("ssl-truststore-create").save();
    clientForTestUser.getSubsystem(TrustStore.class).create()
        .withPem(FileUtils.readFileToString(testData().resolveFile("pem.txt")))
        .save();
  }

  /**
   * Verify that trusted keys can be deleted only when user has "ssl-truststore-delete" permission.
   */
  @Test
  public void delete()
      throws Exception
  {
    role.withPrivilege("ssl-truststore-create").save();
    final Certificate trustedKey = clientForTestUser.getSubsystem(TrustStore.class).create()
        .withPem(FileUtils.readFileToString(testData().resolveFile("pem.txt")))
        .save();

    try {
      trustedKey.remove();
      assertThat("Expected to fail with 403 Forbidden", false);
    }
    catch (UniformInterfaceException e) {
      // expected as user does not have the necessary permissions
      assertThat(e.getResponse().getClientResponseStatus(), Matchers.is(Status.FORBIDDEN));
    }

    role.withPrivilege("ssl-truststore-delete").save();
    trustedKey.remove();
  }

}
