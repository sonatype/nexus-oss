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
package com.sonatype.nexus.testsuite.ldap.nxcm1463;

import com.sonatype.nexus.testsuite.ldap.AbstractLdapIT;
import com.sonatype.security.ldap.api.dto.LdapSchemaTemplateListResponse;

import org.sonatype.nexus.integrationtests.RequestFacade;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Response;

public class NXCM1463TemplateIT
    extends AbstractLdapIT
{

  protected boolean isStartServer() {
    return false;
  }

  @Test
  public void testTemplateList()
      throws Exception
  {
    Response response = null;
    try {
      response = RequestFacade.doGetRequest(RequestFacade.SERVICE_LOCAL + "ldap/templates");
      String responseText = response.getEntity().getText();

      Assert.assertEquals("Response text:\n" + responseText, 200, response.getStatus().getCode());

      // make sure we have the correct number of templates
      LdapSchemaTemplateListResponse templateResponse = this.getFromResponse(
          LdapSchemaTemplateListResponse.class,
          this.getXMLXStream(),
          responseText);
      Assert.assertEquals(4, templateResponse.getData().size());
    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }

}
