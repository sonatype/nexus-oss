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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;

import org.sonatype.nexus.bootstrap.jetty.ConnectorConfiguration;
import org.sonatype.nexus.bootstrap.jetty.ConnectorRegistrar;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;
import org.sonatype.nexus.repository.docker.DockerConnectorFacet;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.validation.ConstraintViolationFactory;
import org.sonatype.nexus.validation.ConstraintViolations;
import org.sonatype.nexus.validation.constraint.PortNumber;

import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConfiguration.Customizer;
import org.eclipse.jetty.server.Request;

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

  private final ConstraintViolationFactory constraintViolationFactory;

  private final ConnectorRegistrar connectorRegistrar;

  private Config config;

  private ConnectorConfiguration httpConfig;

  private ConnectorConfiguration httpsConfig;

  @Inject
  public DockerConnectorFacetImpl(final RepositoryManager repositoryManager,
                                  final ConstraintViolationFactory constraintViolationFactory,
                                  final ConnectorRegistrar connectorRegistrar)
  {
    this.repositoryManager = checkNotNull(repositoryManager);
    this.constraintViolationFactory = checkNotNull(constraintViolationFactory);
    this.connectorRegistrar = checkNotNull(connectorRegistrar);
  }

  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    facet(ConfigurationFacet.class).validateSection(configuration, CONFIG_KEY, Config.class);

    Config configToValidate = facet(ConfigurationFacet.class).readSection(configuration, CONFIG_KEY, Config.class);
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    maybeAdd(violations, validatePortNotInUse(pathOf("httpPort"), configToValidate.httpPort));
    maybeAdd(violations, validatePortNotInUse(pathOf("httpsPort"), configToValidate.httpsPort));
    maybeAdd(violations, validateNotSamePort(
        pathOf("httpPort"), configToValidate.httpPort, pathOf("httpsPort"), configToValidate.httpsPort
    ));
    ConstraintViolations.maybePropagate(violations, log);
  }

  @Override
  protected void doConfigure(final Configuration configuration) throws Exception {
    config = facet(ConfigurationFacet.class).readSection(configuration, CONFIG_KEY, Config.class);
    log.debug("Config: {}", config);
  }

  @Override
  protected void doStart() throws Exception {
    if (config.httpPort != null) {
      try {
        httpConfig = new DockerConnectorConfiguration(getRepository().getName(), HttpScheme.HTTP, config.httpPort);
        connectorRegistrar.addConnector(httpConfig);
      }
      catch (Exception e) {
        log.warn(
            "Could not configure HTTP connector on port {} for docker repository {}",
            config.httpPort, getRepository().getName(), e
        );
      }
    }
    if (config.httpsPort != null) {
      try {
        httpsConfig = new DockerConnectorConfiguration(getRepository().getName(), HttpScheme.HTTPS, config.httpsPort);
        connectorRegistrar.addConnector(httpsConfig);
      }
      catch (Exception e) {
        log.warn(
            "Could not configure HTTPS connector on port {} for docker repository {}",
            config.httpsPort, getRepository().getName(), e
        );
      }
    }
  }

  @Override
  protected void doStop() throws Exception {
    if (httpConfig != null) {
      connectorRegistrar.removeConnector(httpConfig);
    }
    if (httpsConfig != null) {
      connectorRegistrar.removeConnector(httpsConfig);
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

  /**
   * Validate that port is not used as used as HTTP/HTTPS ports in another repository.
   */
  private ConstraintViolation<?> validatePortNotInUse(final String path, final Integer port) {
    if (port != null) {
      Iterable<Repository> repositories;
      try {
        repositories = repositoryManager.browse();
      }
      catch (IllegalStateException ignored) { //NOSONAR
        // this will happen when facet is validated during startup of repository manager (load from storage)
        // ignore and consider configurations loaded from storage as valid
        repositories = Collections.emptyList();
      }
      for (Repository repository : repositories) {
        if (DockerFormat.NAME.equals(repository.getFormat().getValue()) && !repository.equals(getRepository())) {
          DockerConnectorFacet connectorFacet = repository.facet(DockerConnectorFacet.class);
          if (port.equals(connectorFacet.getHttpPort()) || port.equals(connectorFacet.getHttpsPort())) {
            return constraintViolationFactory.createViolation(
                path, "Port must be unique (conflicts with repository'" + repository.getName() + "')"
            );
          }
        }
      }
    }
    return null;
  }

  /**
   * Validate that HTTP and HTTPS ports are not equal.
   */
  private ConstraintViolation<?>[] validateNotSamePort(final String httpPortPath,
                                                       final Integer httpPort,
                                                       final String httpsPortPath,
                                                       final Integer httpsPort)
  {
    if (httpPort != null && httpPort.equals(httpsPort)) {
      return new ConstraintViolation<?>[]{
          constraintViolationFactory.createViolation(
              httpPortPath, "HTTP and HTTPs ports must be different"
          ),
          constraintViolationFactory.createViolation(
              httpsPortPath, "HTTP and HTTPs ports must be different"
          )
      };
    }
    return new ConstraintViolation<?>[]{};
  }


  private static String pathOf(final String fieldName) {
    return StorageFacet.P_ATTRIBUTES + "." + CONFIG_KEY + "." + fieldName;
  }

  private static void maybeAdd(final Set<ConstraintViolation<?>> violations, final ConstraintViolation<?>... toAdd)
  {
    for (ConstraintViolation<?> violation : toAdd) {
      if (violation != null) {
        violations.add(violation);
      }
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

  private static class DockerConnectorConfiguration
      implements ConnectorConfiguration
  {
    private final String repositoryName;

    private final HttpScheme scheme;

    private final int port;

    private DockerConnectorConfiguration(final String repositoryName, final HttpScheme scheme, final int port) {
      this.repositoryName = repositoryName;
      this.scheme = scheme;
      this.port = port;
    }

    @Override
    public HttpScheme getScheme() {
      return scheme;
    }

    @Override
    public int getPort() {
      return port;
    }

    /**
     * Rewrite /v1/* to /repository{repository name}/v1/*.
     */
    @Override
    public HttpConfiguration customize(final HttpConfiguration configuration) {
      configuration.addCustomizer(new Customizer()
      {
        @Override
        public void customize(final Connector connector,
                              final HttpConfiguration channelConfig,
                              final Request request)
        {
          HttpURI uri = request.getUri();
          if (uri.getPath().startsWith("/v1")) {
            request.setUri(new HttpURI(uri.toString().replaceFirst("/v1", "/repository/" + repositoryName + "/v1")));
            request.setRequestURI(request.getUri().getPathAndParam());
          }
        }
      });
      return configuration;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "repositoryName=" + repositoryName +
          ", scheme=" + scheme +
          ", port=" + port +
          '}';
    }
  }
}
