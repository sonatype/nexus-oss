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
package org.sonatype.nexus.internal.httpclient;

import java.io.IOException;

import org.sonatype.nexus.httpclient.HttpClientFactory;
import org.sonatype.nexus.httpclient.Page.PageContext;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A context of page requests made on behalf of a Repository.
 */
public class RepositoryPageContext
    extends PageContext
{
  private final ProxyRepository proxyRepository;

  public RepositoryPageContext(final HttpClient httpClient, final ProxyRepository proxyRepository) {
    super(httpClient);
    this.proxyRepository = checkNotNull(proxyRepository);
  }

  protected ProxyRepository getProxyRepository() {
    return proxyRepository;
  }

  /**
   * Equips context with repository.
   */
  @Override
  public HttpContext createHttpContext(final HttpUriRequest httpRequest)
      throws IOException
  {
    final HttpContext httpContext = super.createHttpContext(httpRequest);
    httpContext.setAttribute(HttpClientFactory.HTTP_CTX_KEY_REPOSITORY, getProxyRepository());
    return httpContext;
  }
}
