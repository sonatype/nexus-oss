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

import java.util.ArrayList;
import java.util.List;

import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.Configuration;

import com.google.inject.Binder;
import com.google.inject.name.Names;

public class ResourceMergingConfigurationManagerTest
    extends AbstractSecurityTestCase
{
  private ConfigurationManager manager;

  @Override
  protected Configuration getSecurityModelConfig() {
    return ResourceMergingConfigurationManagerTestSecurity.securityModel();
  }

  @Override
  public void configure(Binder binder) {
    super.configure(binder);
    binder.bind(StaticSecurityResource.class).annotatedWith(Names.named("s2")).to(StaticSecurityResource2.class);
    binder.bind(StaticSecurityResource.class).annotatedWith(Names.named("s1")).to(StaticSecurityResource1.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    manager = lookup(ConfigurationManager.class);
  }

  public void testRoleMerging() throws Exception {
    List<CRole> roles = manager.listRoles();

    CRole anon = manager.readRole("anon");
    assertTrue("roles: " + anon.getRoles(), anon.getRoles().contains("other"));
    assertTrue("roles: " + anon.getRoles(), anon.getRoles().contains("role2"));
    assertEquals("roles: " + anon.getRoles(), 2, anon.getRoles().size());

    assertTrue(anon.getPrivileges().contains("priv1"));
    assertTrue(anon.getPrivileges().contains("4-test"));
    assertEquals("privs: " + anon.getPrivileges(), 2, anon.getPrivileges().size());

    assertEquals("Test Anon Role", anon.getName());
    assertEquals("Test Anon Role Description", anon.getDescription());

    CRole other = manager.readRole("other");
    assertTrue(other.getRoles().contains("role2"));
    assertEquals("roles: " + other.getRoles(), 1, other.getRoles().size());

    assertTrue(other.getPrivileges().contains("6-test"));
    assertTrue(other.getPrivileges().contains("priv2"));
    assertEquals("privs: " + other.getPrivileges(), 2, other.getPrivileges().size());

    assertEquals("Other Role", other.getName());
    assertEquals("Other Role Description", other.getDescription());

    // all roles
    assertEquals(8, roles.size());

  }

  public void testPrivsMerging() throws Exception {
    List<CPrivilege> privs = manager.listPrivileges();

    CPrivilege priv = manager.readPrivilege("1-test");
    assertTrue(priv != null);

    priv = manager.readPrivilege("2-test");
    assertTrue(priv != null);

    priv = manager.readPrivilege("4-test");
    assertTrue(priv != null);

    priv = manager.readPrivilege("5-test");
    assertTrue(priv != null);

    priv = manager.readPrivilege("6-test");
    assertTrue(priv != null);

    assertNotNull(manager.readPrivilege("priv1"));
    assertNotNull(manager.readPrivilege("priv2"));
    assertNotNull(manager.readPrivilege("priv3"));
    assertNotNull(manager.readPrivilege("priv4"));
    assertNotNull(manager.readPrivilege("priv5"));

    assertEquals("privs: " + this.privilegeListToStringList(privs), 10, privs.size());
  }

  private List<String> privilegeListToStringList(List<CPrivilege> privs) {
    List<String> ids = new ArrayList<String>();

    for (CPrivilege priv : privs) {
      ids.add(priv.getId());
    }

    return ids;
  }
}
