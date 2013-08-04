/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.testsuite.pr.nexus5026;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.sonatype.jira.AttachmentHandler;
import org.sonatype.jira.mock.MockAttachmentHandler;
import org.sonatype.jira.mock.StubJira;
import org.sonatype.jira.test.JiraXmlRpcTestServlet;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.testsuite.proxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.tests.http.server.jetty.impl.JettyServerProvider;

import com.google.common.io.Files;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class Nexus5026GenerateProblemReportViaWebProxyIT
    extends AbstractNexusWebProxyIntegrationTest
{

  private JettyServerProvider jettyServer;

  @Before
  public void setupJiraMock()
      throws Exception
  {
    setupMockJira();
  }

  @After
  public void shutdownJiraMock()
      throws Exception
  {
    if (jettyServer != null) {
      jettyServer.stop();
    }
  }

  @Test
  public void generateReport()
      throws Exception
  {
    ErrorReportResponse response = ErrorReportUtil.generateProblemReport("sometitle", "somedescription");

    Assert.assertNotNull(response);
    Assert.assertNotNull(response.getData().getJiraUrl());

    assertThat(
        server.getAccessedUris(),
        hasItem(jettyServer.getUrl().toExternalForm() + "/rpc/xmlrpc")
    );
    assertThat(
        server.getAccessedUris(),
        hasItem(jettyServer.getUrl().toExternalForm() + "/rest/api/latest/issue/SBOX-1/attachments")
    );
  }

  private void setupMockJira()
      throws Exception
  {

    final int port = Integer.parseInt(TestProperties.getString("jira-server-port"));

    final File mockDb = getTestFile("jira-mock.db");

    StubJira mock = new StubJira();
    mock.setDatabase(
        Files.toString(mockDb, Charset.forName("utf-8"))
    );

    // we have to give a real version (set in DB and here), because either nexus freaks out otherwise
    MockAttachmentHandler handler = new MockAttachmentHandler();
    handler.setSupportedVersion("4.3");

    handler.setMock(mock);
    List<AttachmentHandler> handlers = Arrays.<AttachmentHandler>asList(handler);
    jettyServer = new JettyServerProvider();
    jettyServer.setPort(port);
    jettyServer.addServlet(new JiraXmlRpcTestServlet(mock, jettyServer.getUrl(), handlers));
    jettyServer.start();
  }

}
