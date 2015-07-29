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
package org.sonatype.nexus.repository.docker.internal;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;

import org.sonatype.nexus.bootstrap.jetty.JettyServer;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;
import org.sonatype.nexus.repository.docker.DockerConnectorFacet;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.validation.ConstraintViolations;
import org.sonatype.nexus.validation.ManualValidator;
import org.sonatype.nexus.validation.constraint.PortNumber;

import com.google.common.base.Throwables;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
@Named
public class DockerConnectorFacetImpl
    extends FacetSupport
    implements DockerConnectorFacet
{
  static final String CONFIG_KEY = "docker";

  private final RepositoryManager repositoryManager;

  private final ManualValidator manualValidator;

  private final RepositoryReverseProxyFilter repositoryReverseProxyFilter;

  private Config config;

  private ServerConnector jettyConnector;

  @Inject
  public DockerConnectorFacetImpl(final RepositoryManager repositoryManager,
                                  final ManualValidator manualValidator,
                                  final RepositoryReverseProxyFilter repositoryReverseProxyFilter)
  {
    this.repositoryManager = checkNotNull(repositoryManager);
    this.manualValidator = checkNotNull(manualValidator);
    this.repositoryReverseProxyFilter = repositoryReverseProxyFilter;
  }

  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    facet(ConfigurationFacet.class).validateSection(configuration, CONFIG_KEY, Config.class);
    Config config = facet(ConfigurationFacet.class).readSection(configuration, CONFIG_KEY, Config.class);
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    maybeAdd(violations, validatePort(pathOf("httpPort"), config.httpPort));
    maybeAdd(violations, validatePort(pathOf("httpsPort"), config.httpsPort));
    ConstraintViolations.maybePropagate(violations, log);
  }

  @Override
  protected void doConfigure(final Configuration configuration) throws Exception {
    config = facet(ConfigurationFacet.class).readSection(configuration, CONFIG_KEY, Config.class);
    log.debug("Config: {}", config);
  }

  @Override
  protected void doStart() throws Exception {
    // FIXME remove once adding docker connectors properly implemented
    new Thread(new Runnable()
    {
      @Override
      public void run() {
        try {
          Server server = JettyServer.getServer();
          jettyConnector = new ServerConnector(server);
          jettyConnector.setPort(config.httpPort);
          server.addConnector(jettyConnector);
          jettyConnector.start();
          repositoryReverseProxyFilter.addRepository(getHttpPort(), getRepository().getName());
        }
        catch (Exception e) {
          throw Throwables.propagate(e);
        }
      }
    }, "start-connector-" + getRepository().getName()).start();
  }

  @Override
  protected void doStop() throws Exception {
    if (jettyConnector != null) {
      repositoryReverseProxyFilter.removeRepository(getHttpPort());
      jettyConnector.stop();
      Server server = JettyServer.getServer();
      server.removeConnector(jettyConnector);
    }
  }

  @Override
  public Integer getHttpPort() {
    return config.httpPort;
  }

  @Override
  public Integer getHttpsPort() {
    return config.httpsPort;
  }

  private ConstraintViolation<?> validatePort(final String path, final Integer port) {
    if (port != null) {
      try {
        for (Repository repository : repositoryManager.browse()) {
          if (DockerFormat.NAME.equals(repository.getFormat().getValue()) && !repository.equals(getRepository())) {
            DockerConnectorFacet connectorFacet = repository.facet(DockerConnectorFacet.class);
            if (port.equals(connectorFacet.getHttpPort()) || port.equals(connectorFacet.getHttpsPort())) {
              return manualValidator.createViolation(
                  path, "Port must be unique (conflicts with repository'" + repository.getName() + "')"
              );
            }
          }
        }
      }
      catch (IllegalStateException ignore) {
        // this will happen when facet is first validated, before repository manager is yet started
      }
    }
    return null;
  }

  private static String pathOf(final String fieldName) {
    return StorageFacet.P_ATTRIBUTES + "." + CONFIG_KEY + "." + fieldName;
  }

  private static void maybeAdd(final Set<ConstraintViolation<?>> violations,
                               final ConstraintViolation<?> violation)
  {
    if (violation != null) {
      violations.add(violation);
    }
  }

  static class Config
  {
    @PortNumber
    public Integer httpPort;

    @PortNumber
    public Integer httpsPort;

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "httpPort=" + httpPort +
          ", httpsPort=" + httpsPort +
          '}';
    }
  }
}
