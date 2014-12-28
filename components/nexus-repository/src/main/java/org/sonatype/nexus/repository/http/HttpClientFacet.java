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
import org.sonatype.nexus.repository.config.ConfigurationAttributes;
import org.sonatype.nexus.repository.view.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A facet that provides an HttpClient for use in constructing remote repositories.
 *
 * TODO: This is a trivial implementation, and should grow to incorporate something like the work Alin did for
 * configuring http in component sources.
 *
 * @since 3.0
 */
public class HttpClientFacet
    extends FacetSupport
{
  public static final String CONFIG_KEY = "http_facet";

  public static final String REMOTE_URL = "remote_url";

  private final HttpClientBuilder httpClientBuilder;

  private final HttpPayloadResponseBuilder payloadResponseBuilder;

  private String remoteUrlBase;

  private CloseableHttpClient httpClient;

  @Inject
  public HttpClientFacet(final HttpClientBuilder httpClientBuilder,
                         final HttpPayloadResponseBuilder payloadResponseBuilder)
  {
    this.payloadResponseBuilder = payloadResponseBuilder;
    this.httpClientBuilder = checkNotNull(httpClientBuilder);
  }

  @Override
  protected void doInit(final Repository repository) throws Exception {
    final Configuration configuration = repository.getConfiguration();

    final ConfigurationAttributes attributes = configuration.attributes(CONFIG_KEY);
    final String remoteUrl = (String) attributes.get(REMOTE_URL);

    this.remoteUrlBase = remoteUrl;
  }

  /**
   * TODO: What's the right abstraction for the responses?
   */
  public Response getResponse(String uriSuffix) throws IOException {
    final String uri = remoteUrlBase + uriSuffix;

    final HttpGet httpGet = new HttpGet(uri);

    final CloseableHttpResponse response = httpClient.execute(httpGet);

    return payloadResponseBuilder.buildPayloadResponse(response);
  }
}
