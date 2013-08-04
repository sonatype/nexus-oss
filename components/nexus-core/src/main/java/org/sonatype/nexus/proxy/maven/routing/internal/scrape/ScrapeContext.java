/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.proxy.maven.routing.internal.scrape;

import java.io.IOException;

import org.sonatype.nexus.apachehttpclient.Hc4Provider;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.routing.PrefixSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.BasicHttpContext;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Request for scraping.
 *
 * @author cstamas
 */
public class ScrapeContext
{
  private final MavenProxyRepository remoteRepository;

  private final String remoteRepositoryRootUrl;

  private final HttpClient httpClient;

  private final int scrapeDepth;

  private boolean stopped;

  private PrefixSource prefixSource;

  private String message;

  /**
   * Constructor, none of the parameters might be {@code null}.
   */
  public ScrapeContext(final MavenProxyRepository remoteRepository, final HttpClient httpClient,
                       final int scrapeDepth)
  {
    this.remoteRepository = checkNotNull(remoteRepository);
    this.remoteRepositoryRootUrl = checkNotNull(remoteRepository.getRemoteUrl());
    this.httpClient = checkNotNull(httpClient);
    this.scrapeDepth = checkNotNull(scrapeDepth);
    this.stopped = false;
  }

  /**
   * Marks the context to be stopped, with successful outcome (when scraping succeeded).
   */
  public void stop(final PrefixSource prefixSource, final String message) {
    this.stopped = true;
    this.prefixSource = checkNotNull(prefixSource);
    this.message = checkNotNull(message);
  }

  /**
   * Marks the context to be stopped, with unsuccessful outcome (when scraping not possible or should be avoided,
   * like
   * remote is detected as MRM Proxy).
   */
  public void stop(final String message) {
    this.stopped = true;
    this.prefixSource = null;
    this.message = checkNotNull(message);
  }

  /**
   * Is this context stopped or not.
   *
   * @return {@code true} if context is stopped, not other {@link Scraper} should be invoked with this context.
   */
  public boolean isStopped() {
    return stopped;
  }

  /**
   * Is this context stopped with successful outcome or not.
   *
   * @return {@code true} if context is stopped with successful outcome.
   */
  public boolean isSuccessful() {
    return isStopped() && prefixSource != null;
  }

  /**
   * The {@link PrefixSource} if scraping succeeded.
   *
   * @return scraped entries or {@code null}.
   */
  public PrefixSource getPrefixSource() {
    return prefixSource;
  }

  /**
   * The last message of {@link Scraper}.
   *
   * @return message.
   */
  public String getMessage() {
    return message;
  }

  // ==

  /**
   * Executes a {@link HttpUriRequest} on behalf of this context.
   *
   * @return the {@link HttpResponse} of the request.
   */
  public HttpResponse executeHttpRequest(final HttpUriRequest httpRequest)
      throws ClientProtocolException, IOException
  {
    final BasicHttpContext httpContext = new BasicHttpContext();
    httpContext.setAttribute(Hc4Provider.HTTP_CTX_KEY_REPOSITORY, remoteRepository);
    return httpClient.execute(httpRequest, httpContext);
  }

  /**
   * The remote repository root URL.
   *
   * @return root url to scrape.
   */
  public String getRemoteRepositoryRootUrl() {
    return remoteRepositoryRootUrl;
  }

  /**
   * The needed depth for scraping.
   *
   * @return the depth.
   */
  public int getScrapeDepth() {
    return scrapeDepth;
  }
}
