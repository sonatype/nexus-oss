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
package org.sonatype.nexus.repository.http;

import javax.inject.Inject;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.util.NestedAttributesMap;

import org.apache.http.client.HttpClient;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link HttpClientFacet} implementation.
 *
 * @since 3.0
 */
public class HttpClientFacetImpl
    extends FacetSupport
  implements HttpClientFacet
{
  public static final String CONFIG_KEY = "httpclient";

  private final HttpClientFactory httpClientFactory;

  private final HttpClientConfigMarshaller marshaller;

  private HttpClient httpClient;

  @Inject
  public HttpClientFacetImpl(final HttpClientFactory httpClientFactory,
                             final HttpClientConfigMarshaller marshaller)
  {
    this.httpClientFactory = checkNotNull(httpClientFactory);
    this.marshaller = checkNotNull(marshaller);
  }

  @Override
  protected void doInit(final Repository repository) throws Exception {
    Configuration configuration = repository.getConfiguration();
    NestedAttributesMap attributes = configuration.attributes(CONFIG_KEY);
    HttpClientConfig httpClientConfig = marshaller.fromMap(attributes);
    httpClient = httpClientFactory.create(httpClientConfig);
    log.debug("Created HTTP client: {}", httpClient);
  }

  @Override
  protected void doDestroy() throws Exception {
    httpClient = null;
  }

  @Override
  public HttpClient getHttpClient() {
    ensureStarted();
    return httpClient;
  }
}
