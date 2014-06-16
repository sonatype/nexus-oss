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
package com.sonatype.security.ldap.api;

import java.util.ArrayList;
import java.util.List;

import com.sonatype.security.ldap.api.dto.LdapServerOrderRequest;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.plexus.rest.resource.PlexusResource;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.resource.ResourceException;

public class ServerOrderRestTest
    extends AbstractLdapRestTest
{

  @Test
  public void testModifiable()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerOrderPlexusResource");
    Assert.assertTrue(pr.isModifiable());
  }

  @Test
  public void testSuccess()
      throws Exception
  {
    LdapConfigurationManager ldapConfigurationManager = this.lookup(LdapConfigurationManager.class);
    ldapConfigurationManager.deleteLdapServerConfiguration("default");

    // add 2 ldapServers
    CLdapServerConfiguration ldapServer1 = new CLdapServerConfiguration();
    ldapServer1.setName("testSuccess1");
    ldapServer1.setConnectionInfo(this.buildConnectionInfo());
    ldapServer1.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer1);

    CLdapServerConfiguration ldapServer2 = new CLdapServerConfiguration();
    ldapServer2.setName("testSuccess2");
    ldapServer2.setConnectionInfo(this.buildConnectionInfo());
    ldapServer2.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer2);

    CLdapServerConfiguration ldapServer3 = new CLdapServerConfiguration();
    ldapServer3.setName("testSuccess3");
    ldapServer3.setConnectionInfo(this.buildConnectionInfo());
    ldapServer3.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer3);

    CLdapServerConfiguration ldapServer4 = new CLdapServerConfiguration();
    ldapServer4.setName("testSuccess4");
    ldapServer4.setConnectionInfo(this.buildConnectionInfo());
    ldapServer4.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer4);

    // the order at this point is 1, 2, 3, 4
    // we will change it to 3, 1, 4, 2
    List<String> newOrder = new ArrayList<String>();
    newOrder.add(ldapServer3.getId());
    newOrder.add(ldapServer1.getId());
    newOrder.add(ldapServer4.getId());
    newOrder.add(ldapServer2.getId());

    LdapServerOrderRequest orderRequest = new LdapServerOrderRequest();
    orderRequest.setData(newOrder);

    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerOrderPlexusResource");
    List<String> resultNewOrder = ((LdapServerOrderRequest) pr.put(null, null, null, orderRequest)).getData();
    Assert.assertEquals(newOrder, resultNewOrder);

    // check for the same order as above
    List<CLdapServerConfiguration> ldapServers = ldapConfigurationManager.listLdapServerConfigurations();
    Assert.assertEquals(ldapServers.get(0).getId(), ldapServer3.getId());
    Assert.assertEquals(ldapServers.get(1).getId(), ldapServer1.getId());
    Assert.assertEquals(ldapServers.get(2).getId(), ldapServer4.getId());
    Assert.assertEquals(ldapServers.get(3).getId(), ldapServer2.getId());

  }

  @Test
  public void testFailure()
      throws Exception
  {
    LdapConfigurationManager ldapConfigurationManager = this.lookup(LdapConfigurationManager.class);
    ldapConfigurationManager.deleteLdapServerConfiguration("default");

    // add 2 ldapServers
    CLdapServerConfiguration ldapServer1 = new CLdapServerConfiguration();
    ldapServer1.setName("testSuccess1");
    ldapServer1.setConnectionInfo(this.buildConnectionInfo());
    ldapServer1.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer1);

    CLdapServerConfiguration ldapServer2 = new CLdapServerConfiguration();
    ldapServer2.setName("testSuccess2");
    ldapServer2.setConnectionInfo(this.buildConnectionInfo());
    ldapServer2.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer2);

    CLdapServerConfiguration ldapServer3 = new CLdapServerConfiguration();
    ldapServer3.setName("testSuccess3");
    ldapServer3.setConnectionInfo(this.buildConnectionInfo());
    ldapServer3.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer3);

    CLdapServerConfiguration ldapServer4 = new CLdapServerConfiguration();
    ldapServer4.setName("testSuccess4");
    ldapServer4.setConnectionInfo(this.buildConnectionInfo());
    ldapServer4.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer4);

    // the order at this point is 1, 2, 3, 4
    // we will change it to 3, 1, 4, 2
    List<String> newOrder = new ArrayList<String>();
    newOrder.add(ldapServer3.getId());
    newOrder.add(ldapServer1.getId());
    newOrder.add("SOME_ID_THAT_DOES_NOT_EXIST");
    newOrder.add(ldapServer1.getId());

    LdapServerOrderRequest orderRequest = new LdapServerOrderRequest();
    orderRequest.setData(newOrder);

    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerOrderPlexusResource");

    try {
      pr.put(null, null, null, orderRequest);
      Assert.fail("expected ResourceException");
    }
    catch (ResourceException e) {
      // expected
    }

  }

}
