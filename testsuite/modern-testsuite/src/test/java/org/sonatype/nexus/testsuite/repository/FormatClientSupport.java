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
package org.sonatype.nexus.testsuite.repository;

import java.io.IOException;
import java.net.URI;

import javax.annotation.Nonnull;

import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.util.EntityUtils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Support class for HTTP-based repository test clients.
 */
public class FormatClientSupport
    extends ComponentSupport
{
  protected final HttpClient httpClient;

  protected final HttpClientContext httpClientContext;

  protected final URI repositoryBaseUri;

  public FormatClientSupport(final HttpClient httpClient, final HttpClientContext httpClientContext,
                             final URI repositoryBaseUri)
  {
    this.httpClient = checkNotNull(httpClient);
    this.httpClientContext = checkNotNull(httpClientContext);
    this.repositoryBaseUri = checkNotNull(repositoryBaseUri);
  }

  public static String asString(final HttpResponse response) throws IOException {
    final String asString = EntityUtils.toString(response.getEntity());
    return asString;
  }

  /**
   * GET a response from the repository.
   */
  public HttpResponse get(final String path) throws IOException {
    final URI uri = resolve(path);
    final HttpGet get = new HttpGet(uri);
    return execute(get);
  }


  protected HttpResponse execute(final HttpUriRequest request) throws IOException {
    log.info("Requesting {}", request);
    final HttpResponse response = httpClient.execute(request, httpClientContext);
    log.info("Received {}", response);
    return response;
  }

  @Nonnull
  protected URI resolve(final String path) {
    return repositoryBaseUri.resolve(path);
  }

  public static int status(HttpResponse response) {
    checkNotNull(response);
    return response.getStatusLine().getStatusCode();
  }

  public static byte[] bytes(HttpResponse response) throws IOException {
    checkState(response.getEntity() != null);
    return EntityUtils.toByteArray(response.getEntity());
  }
}
