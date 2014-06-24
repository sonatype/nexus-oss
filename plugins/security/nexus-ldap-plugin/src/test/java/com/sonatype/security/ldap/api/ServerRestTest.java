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

import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;
import com.sonatype.security.ldap.persist.LdapServerNotFoundException;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;

public class ServerRestTest
    extends AbstractLdapRestTest
{

  @Test
  public void testModifiable()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerPlexusResource");
    Assert.assertTrue(pr.isModifiable());
  }

  @Test
  public void testGet()
      throws Exception
  {
    LdapConfigurationManager ldapConfigurationManager = this.lookup(LdapConfigurationManager.class);

    // add 2 ldapServers
    CLdapServerConfiguration ldapServer1 = new CLdapServerConfiguration();
    ldapServer1.setName("testGet1");
    ldapServer1.setConnectionInfo(this.buildConnectionInfo());
    ldapServer1.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer1);

    CLdapServerConfiguration ldapServer2 = new CLdapServerConfiguration();
    ldapServer2.setName("testGet2");
    ldapServer2.setConnectionInfo(this.buildConnectionInfo());
    ldapServer2.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer2);

    // now get the second one
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerPlexusResource");
    LdapServerRequest ldapResponse = (LdapServerRequest) pr.get(
        null,
        this.buildRequest(ldapServer1.getId()),
        null,
        null);

    this.compare(ldapResponse.getData(), ldapServer1);
  }

  @Test
  public void testPut()
      throws Exception
  {
    LdapConfigurationManager ldapConfigurationManager = this.lookup(LdapConfigurationManager.class);

    CLdapServerConfiguration ldapServer1 = new CLdapServerConfiguration();
    ldapServer1.setName("testPut");
    ldapServer1.setConnectionInfo(this.buildConnectionInfo());
    ldapServer1.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer1);

    LdapServerRequest ldapRequest = new LdapServerRequest();
    ldapRequest.setData(this.convert(ldapServer1, new LdapServerConfigurationDTO()));

    ldapRequest.getData().setName("testPut-new");
    ldapRequest.getData().getConnectionInfo().setHost("newhost");
    ldapRequest.getData().getUserAndGroupConfig().setEmailAddressAttribute("newEmailAddressAttribute");

    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerPlexusResource");
    Request request = this.buildRequest(ldapServer1.getId());
    LdapServerRequest ldapResponse = (LdapServerRequest) pr.put(
        null,
        request,
        null,
        ldapRequest);

    // update the request with the expected URL, so we can compare
    ldapRequest.getData().setUrl(request.getResourceRef().toString());

    ldapRequest.getData().getConnectionInfo().setSystemPassword(
        encodeBase64((AbstractLdapPlexusResource.FAKE_PASSWORD))
    );
    this.compare(ldapRequest.getData(), ldapResponse.getData());
  }

  @Test
  public void testDelete()
      throws Exception
  {
    LdapConfigurationManager ldapConfigurationManager = this.lookup(LdapConfigurationManager.class);

    // add 2 ldapServers
    CLdapServerConfiguration ldapServer1 = new CLdapServerConfiguration();
    ldapServer1.setName("testDelete1");
    ldapServer1.setConnectionInfo(this.buildConnectionInfo());
    ldapServer1.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer1);

    CLdapServerConfiguration ldapServer2 = new CLdapServerConfiguration();
    ldapServer2.setName("testDelete2");
    ldapServer2.setConnectionInfo(this.buildConnectionInfo());
    ldapServer2.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer2);

    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerPlexusResource");
    pr.delete(null, this.buildRequest(ldapServer1.getId()), null);
    try {
      ldapConfigurationManager.getLdapServerConfiguration(ldapServer1.getId());
      Assert.fail("epected LdapServerNotFoundException");
    }
    catch (LdapServerNotFoundException e) {
      // expected
    }

    pr.delete(null, this.buildRequest(ldapServer2.getId()), null);
    try {
      ldapConfigurationManager.getLdapServerConfiguration(ldapServer1.getId());
      Assert.fail("epected LdapServerNotFoundException");
    }
    catch (LdapServerNotFoundException e) {
      // expected
    }

    try {
      pr.delete(null, this.buildRequest(ldapServer2.getId()), null);
    }
    catch (ResourceException e) {
      Assert.assertEquals(404, e.getStatus().getCode());
    }

  }

  private <T, F> T convert(F from, T to) {
    // xstream cheat ahead
    XStream xstream = new XStream();
    xstream.setClassLoader(Thread.currentThread().getContextClassLoader());
    String fromXml = xstream.toXML(from);
    to = (T) xstream.fromXML(fromXml, to);
    return to;
  }
}
