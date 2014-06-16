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

import com.sonatype.security.ldap.AbstractLdapTestCase;
import com.sonatype.security.ldap.api.dto.LdapAuthenticationTestRequest;
import com.sonatype.security.ldap.api.dto.LdapConnectionInfoDTO;

import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.sisu.litmus.testsupport.group.Slow;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;

@Category(Slow.class)
public class ServerConnectionRestTest
    extends AbstractLdapTestCase
{

  @Test
  public void testModifiable()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapTestAuthenticationPlexusResource");
    Assert.assertTrue(pr.isModifiable());
  }

  @Test
  public void testSuccess()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapTestAuthenticationPlexusResource");

    LdapAuthenticationTestRequest authTestRequest = new LdapAuthenticationTestRequest();
    LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
    authTestRequest.setData(dto);

    dto.setAuthScheme("simple");
    dto.setHost("localhost");
    dto.setPort(this.getLdapServer("default").getPort());
    dto.setProtocol("ldap");
    dto.setSystemPassword(encodeBase64("secret"));
    dto.setSystemUsername(encodeBase64("uid=admin,ou=system"));
    dto.setSearchBase("o=sonatype");

    Request request = new Request();
    Response response = new Response(request);

    pr.put(null, request, response, authTestRequest);

    Assert.assertEquals(204, response.getStatus().getCode());
  }

  @Test
  public void testFailure()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapTestAuthenticationPlexusResource");

    LdapAuthenticationTestRequest authTestRequest = new LdapAuthenticationTestRequest();
    LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
    authTestRequest.setData(dto);

    dto.setAuthScheme("simple");
    dto.setHost("invalidHost");
    dto.setPort(this.getLdapServer("default").getPort());
    dto.setProtocol("ldap");
    dto.setSystemPassword(encodeBase64("secret"));
    dto.setSystemUsername(encodeBase64("uid=admin,ou=system"));
    dto.setSearchBase("o=sonatype");

    Request request = new Request();
    Response response = new Response(request);

    try {
      pr.put(null, request, response, authTestRequest);
      Assert.fail("expected ResourceException");
    }
    catch (ResourceException e) {
      // expected
    }
  }

}
