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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The page fetched from remote and preprocessed by JSoup.
 *
 * @author cstamas
 * @since 2.4
 */
public class Page
{
  private final String url;

  private final HttpResponse httpResponse;

  private final Document document;

  /**
   * Constructor.
   *
   * @param url          the URL from where this page was fetched.
   * @param httpResponse the HTTP response for this page (with consumed body!).
   * @param document     the JSoup document for this page or {@code null} if no body.
   */
  public Page(final String url, final HttpResponse httpResponse, final Document document) {
    this.url = checkNotNull(url);
    this.httpResponse = checkNotNull(httpResponse);
    this.document = document;
  }

  /**
   * The URL from where this page was fetched.
   *
   * @return the URL from where this page was fetched.
   */
  public String getUrl() {
    return url;
  }

  /**
   * The HTTP response for this page (response body is consumed!). To check stuff like headers.
   *
   * @return the HTTP response of page.
   */
  public HttpResponse getHttpResponse() {
    return httpResponse;
  }

  /**
   * The body of the page, parsed by JSoup, or {@code null} if server did not sent any body.
   *
   * @return the page body document if any, or {@code null}.
   */
  public Document getDocument() {
    return document;
  }

  // ==

  /**
   * Checks if header with given name is present.
   *
   * @return {@code true} if header with given name is present.
   */
  protected boolean hasHeader(final String headerName) {
    return getHttpResponse().getFirstHeader(headerName) != null;
  }

  /**
   * Checks if header with given name is present and start with given value.
   *
   * @return {@code true} if header with given name is present and starts with given value.
   */
  protected boolean hasHeaderAndStartsWith(final String headerName, final String value) {
    final Header header = getHttpResponse().getFirstHeader(headerName);
    return header != null && header.getValue() != null && header.getValue().startsWith(value);
  }

  /**
   * Checks if header with given name is present and equals with given value.
   *
   * @return {@code true} if header with given name is present and equals with given value.
   */
  protected boolean hasHeaderAndEqualsWith(final String headerName, final String value) {
    final Header header = getHttpResponse().getFirstHeader(headerName);
    return header != null && header.getValue() != null && header.getValue().equals(value);
  }

  // ==

  private static final Logger LOG = LoggerFactory.getLogger(Page.class);

  /**
   * Returns a page for given URL.
   *
   * @return the Page for given URL.
   */
  public static Page getPageFor(final ScrapeContext context, final String url)
      throws IOException
  {
    checkNotNull(context);
    checkNotNull(url);
    // TODO: detect redirects
    final HttpGet get = new HttpGet(url);
    LOG.debug("Executing HTTP GET request against {}", url);
    final HttpResponse response = context.executeHttpRequest(get);
    try {
      if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 499) {
        if (response.getEntity() != null) {
          return new Page(url, response, Jsoup.parse(response.getEntity().getContent(), null, url));
        }
        else {
          // no body
          return new Page(url, response, null);
        }
      }
      else {
        throw new UnexpectedPageResponse(url, response.getStatusLine());
      }
    }
    finally {
      EntityUtils.consumeQuietly(response.getEntity());
    }
  }

  /**
   * Exception thrown when unexpected response code is received from page. Used to distinguish between this case and
   * other "real" IO problems like connectivity or transport problems. Not every scraper will want to handle this
   * either, but in cases when target is recognized, but during scrape unexpected response is hit, something the
   * scraping chain must be stopped. See {@link AmazonS3IndexScraper} for an example.
   */
  @SuppressWarnings("serial")
  public static class UnexpectedPageResponse
      extends IOException
  {
    private final String url;

    private final StatusLine statusLine;

    /**
     * Constructor.
     */
    public UnexpectedPageResponse(final String url, final StatusLine statusLine) {
      super("Unexpected response from remote repository URL " + url + " : " + statusLine);
      this.url = url;
      this.statusLine = statusLine;
    }

    /**
     * The full URL that emitted the response.
     *
     * @return the URL
     */
    public String getUrl() {
      return url;
    }

    /**
     * The status line (code and reason phrase) that was unexpected.
     *
     * @return the status line of response.
     */
    public StatusLine getStatusLine() {
      return statusLine;
    }
  }
}
