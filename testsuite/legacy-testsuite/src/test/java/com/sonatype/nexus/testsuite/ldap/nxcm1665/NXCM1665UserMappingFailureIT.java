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

import java.util.Locale;

import com.sonatype.nexus.testsuite.ldap.AbstractLdapIT;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;

import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;

import com.thoughtworks.xstream.XStream;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.respondsWithStatusCode;

public class NXCM1665UserMappingFailureIT
    extends AbstractLdapIT
{

  @Test
  public void testLdapServerNotStartedGivesBadRequest()
      throws Exception
  {
    LdapServerRequest serverRequest = getDefaultServerRequest();

    XStream xstream = this.getXMLXStream();
    String responseText =
        RequestFacade.doPutForText(RequestFacade.SERVICE_LOCAL + "ldap/test_user_conf",
            new XStreamRepresentation(xstream,
                xstream.toXML(serverRequest),
                MediaType.APPLICATION_XML),
            respondsWithStatusCode(400));

    ErrorResponse errorResponse = this.getFromResponse(ErrorResponse.class, xstream, responseText);
    ErrorMessage errorMessage = (ErrorMessage) errorResponse.getErrors().get(0);
    Assert.assertEquals(errorMessage.getId(), "*");

    // the error should say something about a connection error
    assertThat(errorMessage.getMsg().toLowerCase(Locale.US), Matchers.containsString("connect"));
  }

  protected boolean isStartServer() {
    return false;
  }
}
