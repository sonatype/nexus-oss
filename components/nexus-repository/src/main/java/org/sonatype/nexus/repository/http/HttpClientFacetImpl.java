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

import java.io.IOException;

import javax.inject.Inject;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.nexus.repository.view.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

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
  public static final String CONFIG_KEY = "http_facet";

  public static final String REMOTE_URL = "remote_url";

  private final HttpClientFactory httpClientFactory;

  private final HttpPayloadResponseBuilder payloadResponseBuilder;

  private final HttpClientConfigMarshaller marshaller;

  private String remoteUrlBase;

  private HttpClient httpClient;

  @Inject
  public HttpClientFacetImpl(final HttpClientFactory httpClientFactory,
                             final HttpPayloadResponseBuilder payloadResponseBuilder,
                             final HttpClientConfigMarshaller marshaller)
  {
    this.httpClientFactory = checkNotNull(httpClientFactory);
    this.payloadResponseBuilder = checkNotNull(payloadResponseBuilder);
    this.marshaller = checkNotNull(marshaller);
  }

  @Override
  protected void doInit(final Repository repository) throws Exception {
    final Configuration configuration = repository.getConfiguration();
    final NestedAttributesMap foo = configuration.attributes("httpClientFacet");

    final HttpClientConfig httpClientConfig = marshaller.fromMap(foo);

    httpClient = httpClientFactory.create(httpClientConfig);

    // TODO: This stuff probably doesn't belong in the http client facet

    final NestedAttributesMap attributes = configuration.attributes(CONFIG_KEY);
    final String remoteUrl = (String) attributes.get(REMOTE_URL);

    this.remoteUrlBase = remoteUrl;
  }

  @Override
  public HttpClient getHttpClient() {
    ensureStarted();
    return httpClient;
  }

  /**
   * TODO: What's the right abstraction for the responses?
   */
  public Response getResponse(String uriSuffix) throws IOException {
    final String uri = remoteUrlBase + uriSuffix;

    final HttpGet httpGet = new HttpGet(uri);
    final HttpResponse response = httpClient.execute(httpGet);

    // TODO: Temporary - this cast may be illegal
    return payloadResponseBuilder.buildPayloadResponse((CloseableHttpResponse) response);
  }
}
