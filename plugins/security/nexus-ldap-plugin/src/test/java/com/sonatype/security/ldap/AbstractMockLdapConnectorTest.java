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
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.realms.LdapManager;
import org.sonatype.security.ldap.realms.connector.LdapConnector;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.InterpolationFilterReader;

public abstract class AbstractMockLdapConnectorTest
    extends AbstractLdapConfigurationTest
{

  private EnterpriseLdapManager ldapManager = null;

  public void setUp()
      throws Exception
  {
    super.setUp();

    String filename = "ldap.xml";
    String resourcePath = this.getClass().getName().replace('.', '/');
    resourcePath += "-" + filename;

    if (ClassLoader.getSystemResource(resourcePath) == null) {
      resourcePath = "defaults/" + filename;
    }

    getConfHomeDir().mkdirs();
    Map<String, String> interpolationMap = new HashMap<String, String>();
    interpolationMap.put("default-ldap-port", "12345");

    try (InterpolationFilterReader reader = new InterpolationFilterReader(new InputStreamReader(
        ClassLoader.getSystemResourceAsStream(resourcePath)), interpolationMap);
         OutputStream out = new FileOutputStream(new File(getConfHomeDir(), "ldap.xml"));) {
      IOUtils.copy(reader, out);
    }

    this.ldapManager = (EnterpriseLdapManager) this.lookup(LdapManager.class);
    this.resetLdapConnectors();
  }

  protected void resetLdapConnectors() throws Exception {
    List<LdapConnector> connectors = this.ldapManager.getLdapConnectors();

    connectors.clear();
    connectors.addAll(this.getLdapConnectors());
  }

  protected abstract List<LdapConnector> getLdapConnectors();

  protected MockLdapConnector buildMainMockServer(String serverId) {
    SortedSet<String> groupIds = new TreeSet<String>();
    groupIds.add("alpha");
    groupIds.add("beta");
    groupIds.add("gamma");

    SortedSet<LdapUser> users = new TreeSet<LdapUser>();
    LdapUser rwalker = new LdapUser();
    rwalker.setDn("uid=rwalker,ou=people,o=sonatype");
    rwalker.setRealName("Robin E. Walker");
    rwalker.setEmail("rwalker@sonatype.com");
    rwalker.setMembership(new HashSet<String>());
    rwalker.getMembership().addAll(groupIds); // has all groups
    rwalker.setPassword("rwalker123");
    rwalker.setUsername("rwalker");
    users.add(rwalker);

    LdapUser ehearn = new LdapUser();
    ehearn.setDn("uid=ehearn,ou=people,o=sonatype");
    ehearn.setRealName("Eula Hearn");
    ehearn.setEmail("ehearn@sonatype.com");
    ehearn.setMembership(new HashSet<String>());
    ehearn.getMembership().add("alpha");
    ehearn.getMembership().add("gamma");
    ehearn.setPassword("ehearn123");
    ehearn.setUsername("ehearn");
    users.add(ehearn);

    LdapUser jgoodman = new LdapUser();
    jgoodman.setDn("uid=jgoodman,ou=people,o=sonatype");
    jgoodman.setRealName("Joseph M. Goodman");
    jgoodman.setEmail("jgoodman@sonatype.com");
    jgoodman.setMembership(new HashSet<String>());
    jgoodman.getMembership().add("alpha");
    jgoodman.getMembership().add("beta");
    jgoodman.setPassword("jgoodman123");
    jgoodman.setUsername("jgoodman");
    users.add(jgoodman);

    return new MockLdapConnector(serverId, users, groupIds);
  }

  protected MockLdapConnector buildBackupMockServer(String serverId) {
    SortedSet<String> groupIds = new TreeSet<String>();
    groupIds.add("alpha");
    groupIds.add("beta");

    SortedSet<LdapUser> users = new TreeSet<LdapUser>();
    LdapUser ehearn = new LdapUser();
    ehearn.setDn("uid=ehearn,ou=people,o=sonatype");
    ehearn.setRealName("Eula Hearn");
    ehearn.setEmail("ehearn@sonatype.com");
    ehearn.setMembership(new HashSet<String>());
    ehearn.getMembership().add("alpha");
    ehearn.setPassword("ehearn123");
    ehearn.setUsername("ehearn");
    users.add(ehearn);

    LdapUser jgoodman = new LdapUser();
    jgoodman.setDn("uid=jgoodman,ou=people,o=sonatype");
    jgoodman.setRealName("Joseph M. Goodman");
    jgoodman.setEmail("jgoodman@sonatype.com");
    jgoodman.setMembership(new HashSet<String>());
    jgoodman.getMembership().add("alpha");
    jgoodman.getMembership().add("beta");
    jgoodman.setPassword("jgoodman123");
    jgoodman.setUsername("jgoodman");
    users.add(jgoodman);

    return new MockLdapConnector(serverId, users, groupIds);
  }

  protected EnterpriseLdapManager getLdapManager() {
    return ldapManager;
  }

  protected void setLdapManager(EnterpriseLdapManager ldapManager) {
    this.ldapManager = ldapManager;
  }

}
