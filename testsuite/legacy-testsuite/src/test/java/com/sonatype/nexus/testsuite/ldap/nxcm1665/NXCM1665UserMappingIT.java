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
package com.sonatype.nexus.testsuite.ldap.nxcm1665;

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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.respondsWithStatusCode;

public class NXCM1665UserMappingIT
    extends AbstractLdapIT
{

  @Test
  public void testUserMapping()
      throws Exception
  {

    LdapServerRequest serverRequest = getDefaultServerRequest();

    XStream xstream = this.getXMLXStream();
    String responseText =
        RequestFacade.doPutForText(RequestFacade.SERVICE_LOCAL + "ldap/test_user_conf",
            new XStreamRepresentation(xstream,
                xstream.toXML(serverRequest),
                MediaType.APPLICATION_XML),
            respondsWithStatusCode(200));

    LdapUserListResponse listResponse = this.getFromResponse(LdapUserListResponse.class, xstream,
        responseText);

    assertThat(listResponse.getData(), hasSize(3));

    // and make sure everyone has groups
    for (LdapUserDTO userDto : listResponse.getData()) {
      assertThat(userDto.getRoles(), not(Matchers.<String>empty()));
    }
  }
}
