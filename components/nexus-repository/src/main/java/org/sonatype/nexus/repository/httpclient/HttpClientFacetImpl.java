/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.repository.httpclient;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;

import com.google.common.annotations.VisibleForTesting;
import org.apache.http.client.HttpClient;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;

/**
 * Default {@link HttpClientFacet} implementation.
 *
 * @since 3.0
 */
@Named
public class HttpClientFacetImpl
    extends FacetSupport
    implements HttpClientFacet
{
  private final HttpClientFactory factory;

  @VisibleForTesting
  static final String CONFIG_KEY = "httpclient";

  private HttpClientConfig config;

  private FilteredHttpClient httpClient;

  @Inject
  public HttpClientFacetImpl(final HttpClientFactory factory) {
    this.factory = checkNotNull(factory);
  }

  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    facet(ConfigurationFacet.class).validateSection(configuration, CONFIG_KEY, HttpClientConfig.class);
  }

  @Override
  protected void doConfigure(final Configuration configuration) throws Exception {
    config = facet(ConfigurationFacet.class).readSection(configuration, CONFIG_KEY, HttpClientConfig.class);
    log.debug("Config: {}", config);

    httpClient = new FilteredHttpClient(factory.create(config), config);
    log.debug("Created HTTP client: {}", httpClient);
  }

  @Override
  protected void doDestroy() throws Exception {
    config = null;
  }

  @Override
  @Guarded(by = STARTED)
  public HttpClient getHttpClient() {
    return checkNotNull(httpClient);
  }

  @Override
  @Guarded(by = STARTED)
  public RemoteConnectionStatus getStatus() {
    return httpClient.getStatus();
  }

  @Override
  protected void doStop() throws Exception {
    log.debug("Closing HTTP client: {}", httpClient);
    httpClient.close();
    httpClient = null;
  }
}
