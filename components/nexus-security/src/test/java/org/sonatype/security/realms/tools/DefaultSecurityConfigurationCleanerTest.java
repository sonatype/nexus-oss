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
package org.sonatype.security.realms.tools;

import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Tests for {@link DefaultSecurityConfigurationCleaner}.
 */
public class DefaultSecurityConfigurationCleanerTest
    extends TestSupport
{
  private DefaultSecurityConfigurationCleaner underTest;

  private Configuration configuration;

  @Before
  public void setUp() throws Exception {
    underTest = new DefaultSecurityConfigurationCleaner();
    configuration = DefaultSecurityConfigurationCleanerTestSecurity.securityModel();
  }

  @Test
  public void testRemovePrivilege() throws Exception {
    String privilegeId = configuration.getPrivileges().get(0).getId();
    configuration.removePrivilege(privilegeId);

    underTest.privilegeRemoved(configuration, privilegeId);

    for (CRole role : configuration.getRoles()) {
      assertFalse(role.getPrivileges().contains(privilegeId));
    }
  }

  @Test
  public void testRemoveRole() throws Exception {
    String roleId = configuration.getRoles().get(0).getId();
    configuration.removeRole(roleId);

    underTest.roleRemoved(configuration, roleId);

    for (CRole crole : configuration.getRoles()) {
      assertFalse(crole.getPrivileges().contains(roleId));
    }

    for (CUserRoleMapping mapping : configuration.getUserRoleMappings()) {
      assertFalse(mapping.getRoles().contains(roleId));
    }
  }
}
