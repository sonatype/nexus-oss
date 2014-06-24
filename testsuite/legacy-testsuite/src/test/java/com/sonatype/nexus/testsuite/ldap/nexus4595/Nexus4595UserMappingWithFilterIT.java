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
package com.sonatype.nexus.testsuite.ldap.nexus4595;

import java.io.IOException;
import java.util.List;

import com.sonatype.nexus.testsuite.ldap.AbstractLdapIT;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;
import com.sonatype.security.ldap.api.dto.LdapUserDTO;
import com.sonatype.security.ldap.api.dto.LdapUserListResponse;

import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.restlet.data.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.respondsWithStatusCode;

/**
 * Verify user filter expression is used when configuring the user mapping over REST.
 */
public class Nexus4595UserMappingWithFilterIT
    extends AbstractLdapIT
{

  /**
   * Verify that the user filter expression is used.
   */
  @Test
  public void testUserMapping()
      throws Exception
  {
    List<LdapUserDTO> data = getUserMapping("description=*Tamas");

    assertThat(data, hasSize(1));
    assertThat(data, contains(hasProperty("userId", is("cstamas"))));

    // and make sure everyone has groups
    for (LdapUserDTO userDto : data) {
      assertThat(userDto.getRoles(), not(Matchers.<String>empty()));
    }
  }

  /**
   * @param ldapFilter
   * @return
   * @throws IOException
   */
  private List<LdapUserDTO> getUserMapping(final String ldapFilter)
      throws IOException
  {
    LdapServerRequest serverRequest = getDefaultServerRequest();

    serverRequest.getData().getUserAndGroupConfig().setLdapFilter(ldapFilter);

    XStream xstream = this.getXMLXStream();
    String responseText = RequestFacade.doPutForText(
        RequestFacade.SERVICE_LOCAL + "ldap/test_user_conf",
        new XStreamRepresentation(xstream, xstream.toXML(serverRequest), MediaType.APPLICATION_XML),
        respondsWithStatusCode(200));

    return this.getFromResponse(LdapUserListResponse.class, xstream, responseText).getData();
  }

}
