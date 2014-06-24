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

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;

public class NXCM1463ServerListJsonIT
    extends AbstractNexusIntegrationTest
{

  @Test
  public void testPost()
      throws Exception
  {
    String json = "{  \ndata\n:  {    \nname\n:\ntest\n,    \nconnectionInfo\n: {     \nsearchBase\n:\ntest\n,    \nsystemUsername\n:null,    \nsystemPassword\n:null,    \nauthScheme\n:\nnone\n,    \nprotocol\n:\nldap\n,      \nhost\n:\ntest\n,      \nport\n:389,   \nbackupMirrorProtocol\n:\nldap\n,      \nbackupMirrorHost\n:\nasdf\n,      \nbackupMirrorPort\n:389,   \nrealm\n:null,     \nconnectionTimeout\n:55,   \nconnectionRetryDelay\n:55,    \ncacheTimeout\n:55   },  \nuserAndGroupConfig\n: {     \nemailAddressAttribute\n:\nasdf\n,     \nldapGroupsAsRoles\n:true,     \ngroupBaseDn\n:null,   \ngroupIdAttribute\n:null,      \ngroupMemberAttribute\n:null,      \ngroupMemberFormat\n:null,     \ngroupObjectClass\n:null,      \nuserPasswordAttribute\n:null,    \nuserIdAttribute\n:\nasdf\n,   \nuserObjectClass\n:\nasdf\n,   \nuserBaseDn\n:null,    \nuserRealNameAttribute\n:\nasdf\n,     \nuserSubtree\n:false,      \ngroupSubtree\n:false,     \nuserMemberOfAttribute\n:\nasdf\n    }  }}";

        /*
         * { \ndata\n: { \nname\n:\ntest\n, \nconnectionInfo\n: { \nsearchBase\n:\ntest\n, \nsystemUsername\n:null,
         * \nsystemPassword\n:null, \nauthScheme\n:\nnone\n, \nprotocol\n:\nldap\n, \nhost\n:\ntest\n, \nport\n:389,
         * \nbackupMirrorProtocol\n:\nldap\n, \nbackupMirrorHost\n:\nasdf\n, \nbackupMirrorPort\n:389, \nrealm\n:null,
         * \nconnectionTimeout\n:55, \nconnectionRetryDelay\n:55, \ncacheTimeout\n:55 }, \nuserAndGroupConfig\n: {
         * \nemailAddressAttribute\n:\nasdf\n, \nldapGroupsAsRoles\n:true, \ngroupBaseDn\n:null,
         * \ngroupIdAttribute\n:null, \ngroupMemberAttribute\n:null, \ngroupMemberFormat\n:null,
         * \ngroupObjectClass\n:null, \nuserPasswordAttribute\n:null, \nuserIdAttribute\n:\nasdf\n,
         * \nuserObjectClass\n:\nasdf\n, \nuserBaseDn\n:null, \nuserRealNameAttribute\n:\nasdf\n, \nuserSubtree\n:false,
         * \ngroupSubtree\n:false, \nuserMemberOfAttribute\n:\nasdf\n } } }
         */

    Response response = null;
    try {
      response = RequestFacade.sendMessage(
          RequestFacade.SERVICE_LOCAL + "ldap/servers",
          Method.POST,
          new StringRepresentation(json, MediaType.APPLICATION_JSON));

      String responseText = response.getEntity().getText();
      Assert.assertEquals("Response Text:\n" + responseText, 201, response.getStatus().getCode());
    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }

}
