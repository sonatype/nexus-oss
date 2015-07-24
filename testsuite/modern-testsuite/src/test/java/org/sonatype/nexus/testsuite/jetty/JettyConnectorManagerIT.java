/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.jetty;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;

import org.sonatype.nexus.jetty.JettyConnectorManager;
import org.sonatype.nexus.testsuite.NexusHttpsITSupport;
import org.sonatype.nexus.testsuite.maven.Maven2Client;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConfiguration.Customizer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * IT for {@link JettyConnectorManager}.
 */
@ExamReactorStrategy(PerClass.class)
public class JettyConnectorManagerIT
    extends NexusHttpsITSupport
{
  @Inject
  private JettyConnectorManager jettyConnectorManager;

  private static final Customizer DUMMY_CUSTOMIZER = new Customizer()
  {
    @Override
    public void customize(final Connector connector, final HttpConfiguration channelConfig, final Request request) {
      request
          .setUri(new HttpURI("http://localhost:1234/repository/maven-central/org/eclipse/jetty/maven-metadata.xml"));
    }
  };

  @Test
  public void testAvailableSchemas() {
    // this is a NexusHttpsITSupport so both HTTP and HTTPS should be present
    List<HttpScheme> supportedSchemes = jettyConnectorManager.supportedSchemes();
    assertThat(supportedSchemes, contains(HttpScheme.HTTP, HttpScheme.HTTPS));
  }

  @Test
  public void testHttpConnectorAdd() throws Exception {
    HttpConfiguration config = jettyConnectorManager.defaultHttpConfiguration(HttpScheme.HTTP);
    config.addCustomizer(DUMMY_CUSTOMIZER);
    final ServerConnector serverConnector = jettyConnectorManager.addConnector(HttpScheme.HTTP, 8082, config);
    serverConnector.start();
    try {
      Maven2Client maven2Client = new Maven2Client(clientBuilder().build(), clientContext(),
          URI.create("http://localhost:8082/"));

      HttpResponse response = maven2Client.get("/whatever/path/it/will/be/rewritten/anyways");
      assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
      String entity = EntityUtils.toString(response.getEntity());
      assertThat(entity, containsString("Jetty :: Jetty JSPC Maven Plugin"));
    }
    finally {
      serverConnector.stop();
    }
  }

  @Test
  public void testHttpsConnectorAdd() throws Exception {
    HttpConfiguration config = jettyConnectorManager.defaultHttpConfiguration(HttpScheme.HTTPS);
    config.addCustomizer(DUMMY_CUSTOMIZER);
    final ServerConnector serverConnector = jettyConnectorManager.addConnector(HttpScheme.HTTPS, 8083, config);
    serverConnector.start();
    try {
      Maven2Client maven2Client = new Maven2Client(clientBuilder().build(), clientContext(),
          URI.create("https://localhost:8083/"));

      HttpResponse response = maven2Client.get("/whatever/path/it/will/be/rewritten/anyways");
      assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
      String entity = EntityUtils.toString(response.getEntity());
      assertThat(entity, containsString("Jetty :: Jetty JSPC Maven Plugin"));
    }
    finally {
      serverConnector.stop();
    }
  }
}
