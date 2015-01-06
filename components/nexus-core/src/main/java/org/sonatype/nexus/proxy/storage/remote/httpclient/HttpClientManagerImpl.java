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
package org.sonatype.nexus.proxy.storage.remote.httpclient;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.httpclient.HttpClientFactory;
import org.sonatype.nexus.httpclient.HttpClientFactory.Builder;
import org.sonatype.nexus.httpclient.NexusRedirectStrategy;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.http.client.HttpClient;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link HttpClientManager}.
 *
 * @author cstamas
 * @since 2.2
 */
@Singleton
@Named
public class HttpClientManagerImpl
    extends ComponentSupport
    implements HttpClientManager
{
  private final HttpClientFactory httpClientFactory;

  @Inject
  public HttpClientManagerImpl(final HttpClientFactory httpClientFactory) {
    this.httpClientFactory = checkNotNull(httpClientFactory);
  }

  @Override
  public HttpClient create(final ProxyRepository proxyRepository, final RemoteStorageContext ctx) {
    checkNotNull(proxyRepository);
    checkNotNull(ctx);
    final Builder builder = httpClientFactory.prepare(new RemoteStorageContextCustomizer(ctx));
    configure(builder);
    return builder.build();
  }

  @Override
  public void release(final ProxyRepository proxyRepository, final RemoteStorageContext ctx) {
    // nop for now
  }

  // ==

  /**
   * Configures the fresh instance of HttpClient for given proxy repository specific needs. Right now it sets
   * appropriate redirect strategy only.
   */
  protected void configure(final Builder builder) {
    // set proxy redirect strategy
    builder.getHttpClientBuilder().setRedirectStrategy(new NexusRedirectStrategy());
  }

}
