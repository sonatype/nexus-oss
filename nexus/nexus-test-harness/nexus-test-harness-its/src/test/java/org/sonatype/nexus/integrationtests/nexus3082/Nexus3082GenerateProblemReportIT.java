/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus3082;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import org.sonatype.jira.AttachmentHandler;
import org.sonatype.jira.mock.MockAttachmentHandler;
import org.sonatype.jira.mock.StubJira;
import org.sonatype.jira.test.JiraXmlRpcTestServlet;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.tests.http.server.api.Behaviour;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.impl.JettyServerProvider;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus3082GenerateProblemReportIT
    extends AbstractNexusIntegrationTest
{

    private JettyServerProvider server;

    @BeforeMethod
    public void setupJiraMock()
        throws Exception
    {
        setupMockJira();
    }

    @AfterMethod
    public void shutdownJiraMock()
        throws Exception
    {
        if ( server != null )
        {
            server.stop();
        }
    }

    @Test
    public void generateReport()
        throws Exception
    {
        ErrorReportResponse response = ErrorReportUtil.generateProblemReport( "sometitle", "somedescription" );

        Assert.assertNotNull( response );

        Assert.assertNotNull( response.getData().getJiraUrl() );
    }

    private void setupMockJira()
        throws Exception
    {

        final int port = Integer.parseInt( TestProperties.getString( "webproxy-server-port" ) );

        setupJiraMock( "jira-mock.db", port );
    }

    private void setupJiraMock( String dbName, int port )
        throws Exception
    {
        final File mockDb = getTestFile( dbName );

        StubJira mock = new StubJira();
        mock.setDatabase(
            Files.toString( mockDb, Charset.forName( "utf-8" ) )
        );

        // we have to give a real version (set in DB and here), because either nexus freaks out otherwise
        MockAttachmentHandler handler = new MockAttachmentHandler();
        handler.setSupportedVersion( "4.3" );

        handler.setMock( mock );
        List<AttachmentHandler> handlers = Arrays.<AttachmentHandler>asList( handler );
        server = new JettyServerProvider();
        server.setPort( port );
        server.addServlet( new JiraXmlRpcTestServlet( mock, server.getUrl(), handlers ) );
        server.start();
    }

    @Test
    public void generateReportWithFailure()
        throws Exception
    {
        ErrorReportUtil.generateProblemReport( null, "somedescription" );
    }

}
