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
package org.sonatype.nexus.security.config;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.security.internal.ConfigurationIdGeneratorImpl;
import org.sonatype.nexus.security.internal.SecurityConfigurationValidatorImpl;
import org.sonatype.nexus.security.privilege.MethodPrivilegeDescriptor;
import org.sonatype.nexus.security.privilege.PrivilegeDescriptor;
import org.sonatype.nexus.validation.ValidationResponse;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultSecurityConfigurationValidatorTest
{
  protected SecurityConfigurationValidator configurationValidator;

  @Before
  public void setUp() throws Exception {
    ConfigurationIdGenerator idGenerator = new ConfigurationIdGeneratorImpl();

    List<PrivilegeDescriptor> privilegeDescriptors = new ArrayList<>();
    MethodPrivilegeDescriptor methodPrivilegeDescriptor = new MethodPrivilegeDescriptor();
    methodPrivilegeDescriptor.installDependencies(idGenerator);
    privilegeDescriptors.add(methodPrivilegeDescriptor);

    configurationValidator = new SecurityConfigurationValidatorImpl(privilegeDescriptors, idGenerator);
  }

  @Test
  public void testBad1() throws Exception {
    ValidationResponse response = configurationValidator
        .validateModel(DefaultSecurityConfigurationValidatorTestSecurity.securityModel1());

    assertFalse(response.isValid());

    assertFalse(response.isModified());

    // emails are not longer unique!
    assertEquals(11, response.getErrors().size());

    assertEquals(0, response.getWarnings().size());
  }

  @Test
  public void testBad2() throws Exception {
    ValidationResponse response = configurationValidator
        .validateModel(DefaultSecurityConfigurationValidatorTestSecurity.securityModel2());

    assertFalse(response.isValid());

    assertTrue(response.isModified());

    assertEquals(3, response.getWarnings().size());

    assertEquals(10, response.getErrors().size());
  }

  @Test
  public void testBad3() throws Exception {
    ValidationResponse response = configurationValidator
        .validateModel(DefaultSecurityConfigurationValidatorTestSecurity.securityModel3());

    assertFalse(response.isValid());

    assertTrue(response.isModified());

    assertEquals(1, response.getWarnings().size());

    assertEquals(2, response.getErrors().size());
  }

  @Test
  public void testRoles() throws Exception {
    SecurityConfigurationValidationContext context = new SecurityConfigurationValidationContext();

    CPrivilege priv = new CPrivilege();
    priv.setId("priv");
    priv.setName("priv");
    priv.setType("invalid");
    context.addExistingPrivilegeIds();
    context.getExistingPrivilegeIds().add("priv");

    CRole role1 = new CRole();
    role1.setId("role1");
    role1.setName("role1");
    role1.setDescription("desc");
    role1.addPrivilege(priv.getId());
    role1.addRole("role2");
    ArrayList<String> containedRoles = new ArrayList<String>();
    containedRoles.add("role2");
    context.addExistingRoleIds();
    context.getExistingRoleIds().add("role1");
    context.getRoleContainmentMap().put("role1", containedRoles);

    CRole role2 = new CRole();
    role2.setId("role2");
    role2.setName("role2");
    role2.setDescription("desc");
    role2.addPrivilege(priv.getId());
    role2.addRole("role3");
    containedRoles = new ArrayList<String>();
    containedRoles.add("role3");
    context.addExistingRoleIds();
    context.getExistingRoleIds().add("role2");
    context.getRoleContainmentMap().put("role2", containedRoles);

    CRole role3 = new CRole();
    role3.setId("role3");
    role3.setName("role3");
    role3.setDescription("desc");
    role3.addPrivilege(priv.getId());
    role3.addRole("role1");
    containedRoles = new ArrayList<String>();
    containedRoles.add("role1");
    context.addExistingRoleIds();
    context.getExistingRoleIds().add("role3");
    context.getRoleContainmentMap().put("role3", containedRoles);

    ValidationResponse vr = configurationValidator.validateRoleContainment(context);

    assertFalse(vr.isValid());
    assertEquals(vr.getErrors().size(), 3);

  }

  /**
   * NEXUS-5040: Creating a role with an unknown privilege should not result in a validation error, just a warning.
   */
  @Test
  public void testValidationOfRoleWithUnknownPrivilege() {
    SecurityConfigurationValidationContext context = new SecurityConfigurationValidationContext();

    CPrivilege priv = new CPrivilege();
    priv.setId("priv");
    priv.setName("priv");
    priv.setType("invalid");
    context.addExistingPrivilegeIds();
    context.getExistingPrivilegeIds().add("priv");

    CRole role1 = new CRole();
    role1.setId("role1");
    role1.setName("role1");
    role1.setDescription("desc");
    role1.addPrivilege(priv.getId());
    role1.addPrivilege("foo");

    context.addExistingRoleIds();
    context.getExistingRoleIds().add("role1");

    ValidationResponse vr = configurationValidator.validateRole(context, role1, true);

    assertTrue(vr.isValid());
    assertEquals(vr.getErrors().size(), 0);
    assertEquals(vr.getWarnings().size(), 1);
    assertEquals(vr.getWarnings().get(0).getMessage(), "Role ID 'role1' Invalid privilege id 'foo' found.");
  }
}
