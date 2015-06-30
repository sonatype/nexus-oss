/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ldap.internal.realms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.sonatype.nexus.ldap.internal.MockLdapConnector;
import org.sonatype.nexus.ldap.internal.connector.LdapConnector;
import org.sonatype.nexus.ldap.internal.connector.dao.LdapUser;

import org.junit.Assert;
import org.junit.Test;

public class MultipleServersSameGroupTest
    extends AbstractMockLdapConnectorTest
{

  private MockLdapConnector oneConnector = null;

  private MockLdapConnector twoConnector = null;

  @Override
  protected Collection<String> getLdapServerNames() {
    return Arrays.asList("default", "one", "two");
  }

  @Override
  protected List<LdapConnector> getLdapConnectors() {
    List<LdapConnector> connectors = new ArrayList<LdapConnector>();
    this.oneConnector = this.buildMainMockServer(ldapClientConfigurations.get("one").getId());
    connectors.add(oneConnector);

    this.twoConnector = this.buildMainMockServerTwo(ldapClientConfigurations.get("two").getId());
    connectors.add(twoConnector);

    return connectors;
  }

  @Test
  public void testLdapManager()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);

    SortedSet<String> groupIds = new TreeSet<String>();
    groupIds.add("alpha");
    groupIds.add("beta");
    groupIds.add("gamma");

    LdapUser jmeis = ldapManager.getUser("jmeis");
    Assert.assertEquals(groupIds, jmeis.getMembership());

    LdapUser rwalker = ldapManager.getUser("rwalker");
    Assert.assertEquals(groupIds, rwalker.getMembership());
  }

  @Test
  public void testLogin()
      throws Exception
  {
    SortedSet<String> groupIds = new TreeSet<String>();
    groupIds.add("alpha");
    groupIds.add("beta");
    groupIds.add("gamma");

    LdapManager ldapManager = this.lookup(LdapManager.class);
    LdapUser jmeis = ldapManager.authenticateUser("jmeis", "jmeis123");
    Assert.assertEquals(groupIds, jmeis.getMembership());

    LdapUser rwalker = ldapManager.authenticateUser("rwalker", "rwalker123");
    Assert.assertEquals(groupIds, rwalker.getMembership());
  }


  protected MockLdapConnector buildMainMockServerTwo(String serverId) {
    SortedSet<String> groupIds = new TreeSet<String>();
    groupIds.add("alpha");
    groupIds.add("beta");
    groupIds.add("gamma");

    SortedSet<LdapUser> users = new TreeSet<LdapUser>();
    LdapUser jmeis = new LdapUser();
    jmeis.setDn("uid=jmeis,ou=people,o=sonatype");
    jmeis.setRealName("James E. Meis");
    jmeis.setEmail("jmeis@sonatype.com");
    jmeis.setMembership(new HashSet<String>());
    jmeis.getMembership().addAll(groupIds); // has all groups
    jmeis.setPassword("jmeis123");
    jmeis.setUsername("jmeis");
    users.add(jmeis);

    LdapUser cdiaz = new LdapUser();
    cdiaz.setDn("uid=diazc,ou=people,o=sonatype");
    cdiaz.setRealName("Candice J. Diaz");
    cdiaz.setEmail("cdiaz@sonatype.com");
    cdiaz.setMembership(new HashSet<String>());
    cdiaz.getMembership().add("alpha");
    cdiaz.getMembership().add("gamma");
    cdiaz.setPassword("cdiaz123");
    cdiaz.setUsername("cdiaz");
    users.add(cdiaz);

    LdapUser rpolk = new LdapUser();
    rpolk.setDn("uid=rpolk,ou=people,o=sonatype");
    rpolk.setRealName("Richard M. Polk");
    rpolk.setEmail("rpolk@sonatype.com");
    rpolk.setMembership(new HashSet<String>());
    rpolk.getMembership().add("alpha");
    rpolk.getMembership().add("beta");
    rpolk.setPassword("rpolk123");
    rpolk.setUsername("rpolk");
    users.add(rpolk);

    return new MockLdapConnector(serverId, users, groupIds);
  }

}
