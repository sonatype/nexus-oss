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

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.util.NestedAttributesMap;

import org.apache.http.client.HttpClient;

import static com.google.common.base.Preconditions.checkNotNull;

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
  public static final String CONFIG_KEY = "httpclient";

  private final HttpClientFactory factory;

  private final HttpClientConfigMarshaller marshaller;

  private HttpClient httpClient;

  @Inject
  public HttpClientFacetImpl(final HttpClientFactory factory,
                             final HttpClientConfigMarshaller marshaller)
  {
    this.factory = checkNotNull(factory);
    this.marshaller = checkNotNull(marshaller);
  }

  @Override
  public HttpClient getHttpClient() {
    return checkNotNull(httpClient);
  }

  @Override
  protected void doInit() throws Exception {
    NestedAttributesMap attributes = getRepository().getConfiguration().attributes(CONFIG_KEY);
    HttpClientConfig config = marshaller.unmarshall(attributes);
    httpClient = factory.create(config);
    log.debug("Created HTTP client: {}", httpClient);
  }

  @Override
  protected void doDestroy() throws Exception {
    httpClient = null;
  }
}
