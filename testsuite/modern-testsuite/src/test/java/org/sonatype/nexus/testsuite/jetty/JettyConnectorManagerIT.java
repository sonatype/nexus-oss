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

import org.sonatype.nexus.bootstrap.jetty.ConnectorConfiguration;
import org.sonatype.nexus.bootstrap.jetty.ConnectorRegistrar;
import org.sonatype.nexus.bootstrap.jetty.UnsupportedHttpSchemeException;
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
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

/**
 * IT for {@link ConnectorConfiguration}.
 */
@ExamReactorStrategy(PerClass.class)
public class JettyConnectorManagerIT
    extends NexusHttpsITSupport
{
  @Inject
  private ConnectorRegistrar connectorRegistrar;

  private static final Customizer DUMMY_CUSTOMIZER = new Customizer()
  {
    @Override
    public void customize(final Connector connector, final HttpConfiguration channelConfig, final Request request) {
      request.setUri(
          new HttpURI("http://localhost:1234/repository/maven-central/org/eclipse/jetty/maven-metadata.xml")
      );
    }
  };

  @Test
  public void availableSchemes() throws Exception {
    final List<HttpScheme> schemes = connectorRegistrar.availableSchemes();
    // This IT enables HTTPS, so must be present
    assertThat(schemes, contains(HttpScheme.HTTP, HttpScheme.HTTPS));
  }

  @Test(expected = UnsupportedHttpSchemeException.class)
  public void unsupportedHttpScheme() throws Exception {
    connectorRegistrar.addConnector(new ConnectorConfiguration()
    {
      @Override
      public HttpScheme getScheme() {
        return HttpScheme.WS;
      }

      @Override
      public int getPort() {
        return 1234;
      }

      @Override
      public HttpConfiguration customize(final HttpConfiguration configuration) {
        return configuration;
      }
    });
  }

  @Test(expected = NullPointerException.class)
  public void nullHttpScheme() throws Exception {
    connectorRegistrar.addConnector(new ConnectorConfiguration()
    {
      @Override
      public HttpScheme getScheme() {
        return null;
      }

      @Override
      public int getPort() {
        return 1234;
      }

      @Override
      public HttpConfiguration customize(final HttpConfiguration configuration) {
        return configuration;
      }
    });
  }

  @Test
  public void unavailablePorts() throws Exception {
    final List<Integer> ports = connectorRegistrar.unavailablePorts();
    // This IT enables HTTPS, so must be present
    assertThat(ports, contains(nexusUrl.getPort(), nexusSecureUrl.getPort()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void usedPort() throws Exception {
    connectorRegistrar.addConnector(new ConnectorConfiguration()
    {
      @Override
      public HttpScheme getScheme() {
        return HttpScheme.HTTP;
      }

      @Override
      public int getPort() {
        return nexusUrl.getPort();
      }

      @Override
      public HttpConfiguration customize(final HttpConfiguration configuration) {
        return configuration;
      }
    });
  }

  @Test(expected = IllegalArgumentException.class)
  public void badPort() throws Exception {
    connectorRegistrar.addConnector(new ConnectorConfiguration()
    {
      @Override
      public HttpScheme getScheme() {
        return HttpScheme.HTTP;
      }

      @Override
      public int getPort() {
        return -1;
      }

      @Override
      public HttpConfiguration customize(final HttpConfiguration configuration) {
        return configuration;
      }
    });
  }

  @Test
  public void doubleRegistration() throws Exception {
    final int port = portRegistry.reservePort();
    try {
      final ConnectorConfiguration config = new ConnectorConfiguration()
      {
        @Override
        public HttpScheme getScheme() {
          return HttpScheme.HTTP;
        }

        @Override
        public int getPort() {
          return port;
        }

        @Override
        public HttpConfiguration customize(final HttpConfiguration configuration) {
          return configuration;
        }
      };
      connectorRegistrar.addConnector(config);
      try {
        connectorRegistrar.addConnector(config);
        fail("Should fail");
      }
      catch (IllegalArgumentException e) {
        // good, is already added
      }
      finally {
        connectorRegistrar.removeConnector(config);
      }
    }
    finally {
      portRegistry.releasePort(port);
    }
  }

  @Test
  public void testHttpConnectorAdd() throws Exception {
    final int newHttpPort = portRegistry.reservePort();

    final ConnectorConfiguration connectorConfiguration = new ConnectorConfiguration()
    {
      @Override
      public HttpScheme getScheme() {
        return HttpScheme.HTTP;
      }

      @Override
      public int getPort() {
        return newHttpPort;
      }

      @Override
      public HttpConfiguration customize(final HttpConfiguration configuration) {
        configuration.addCustomizer(DUMMY_CUSTOMIZER);
        return configuration;
      }
    };
    connectorRegistrar.addConnector(connectorConfiguration);

    try {
      HttpResponse response = new Maven2Client(
          clientBuilder().build(),
          clientContext(),
          URI.create("http://localhost:" + newHttpPort + "/")
      ).get("/whatever/path/it/will/be/rewritten/anyways");
      assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
      String entity = EntityUtils.toString(response.getEntity());
      assertThat(entity, containsString("Jetty :: Jetty JSPC Maven Plugin"));
    }
    finally {
      connectorRegistrar.removeConnector(connectorConfiguration);
      portRegistry.releasePort(newHttpPort);
    }
  }

  @Test
  public void testHttpsConnectorAdd() throws Exception {
    final int newHttpPort = portRegistry.reservePort();

    final ConnectorConfiguration connectorConfiguration = new ConnectorConfiguration()
    {
      @Override
      public HttpScheme getScheme() {
        return HttpScheme.HTTPS;
      }

      @Override
      public int getPort() {
        return newHttpPort;
      }

      @Override
      public HttpConfiguration customize(final HttpConfiguration configuration) {
        configuration.addCustomizer(DUMMY_CUSTOMIZER);
        return configuration;
      }
    };
    connectorRegistrar.addConnector(connectorConfiguration);

    try {
      HttpResponse response = new Maven2Client(
          clientBuilder().build(),
          clientContext(),
          URI.create("https://localhost:" + newHttpPort + "/")
      ).get("/whatever/path/it/will/be/rewritten/anyways");
      assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
      String entity = EntityUtils.toString(response.getEntity());
      assertThat(entity, containsString("Jetty :: Jetty JSPC Maven Plugin"));
    }
    finally {
      connectorRegistrar.removeConnector(connectorConfiguration);
      portRegistry.releasePort(newHttpPort);
    }
  }
}
