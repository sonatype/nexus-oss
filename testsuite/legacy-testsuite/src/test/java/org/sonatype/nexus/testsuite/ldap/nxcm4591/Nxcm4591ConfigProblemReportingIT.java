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

package org.sonatype.nexus.testsuite.ldap.nxcm4591;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.sonatype.jira.AttachmentHandler;
import org.sonatype.jira.mock.MockAttachmentHandler;
import org.sonatype.jira.mock.StubJira;
import org.sonatype.jira.test.JiraXmlRpcTestServlet;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.testsuite.ldap.AbstractLdapIntegrationIT;
import org.sonatype.tests.http.server.jetty.impl.JettyServerProvider;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.codehaus.swizzle.jira.Attachment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Verifies that ldap.xml is included in problem report bundles.
 */
public class Nxcm4591ConfigProblemReportingIT
    extends AbstractLdapIntegrationIT
{

  private JettyServerProvider server;

  private StubJira jira;

  @Before
  public void setupJiraMock()
      throws Exception
  {
    final int port = Integer.parseInt(TestProperties.getString("jira-server-port"));

    final File mockDb = getTestFile("jira-mock.db");

    jira = new StubJira();
    jira.setDatabase(
        Files.toString(mockDb, Charset.forName("utf-8"))
    );

    // we have to give a real version (set in DB and here), because either nexus freaks out otherwise
    // the jira mock
    MockAttachmentHandler handler = new MockAttachmentHandler();
    handler.setSupportedVersion("4.3");

    handler.setMock(jira);
    List<AttachmentHandler> handlers = Arrays.<AttachmentHandler>asList(handler);
    server = new JettyServerProvider();
    server.setPort(port);
    server.addServlet(new JiraXmlRpcTestServlet(jira, server.getUrl(), handlers));
    server.start();
  }

  @After
  public void shutdownJiraMock()
      throws Exception
  {
    if (server != null) {
      server.stop();
    }
  }

  @Test
  public void ldapXmlInBundle()
      throws Exception
  {
    ErrorReportResponse response = ErrorReportUtil.generateProblemReport("sometitle", "somedescription");

    assertThat(response, notNullValue());
    final String url = response.getData().getJiraUrl();
    assertThat(url, notNullValue());

    final Map<Attachment, byte[]> attachments = jira.getAttachments("SBOX-1");
    assertThat(attachments.values(), hasSize(1));
    final byte[] attachment = attachments.values().iterator().next();

    final ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(attachment));
    try {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null && !entry.getName().equals("ldap.xml")) {
        // loop until 'ldap.xml' is found
      }

      assertThat("ldap.xml not found in problem report bundle", entry, notNullValue());

      final String ldapXml = new String(ByteStreams.toByteArray(zip), "utf-8");
      assertThat(ldapXml, containsString("<systemPassword>***</systemPassword>"));
    }
    finally {
      zip.close();
    }
  }

}
