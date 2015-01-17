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
package org.sonatype.nexus.repository.simple.internal;

import java.io.IOException;
import java.net.URI;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.simple.SimpleContent;
import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.nexus.repository.view.payloads.HttpEntityPayload;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;

// HACK: For now implement based on sub-class to get proxy working, then refactor to be interceptor friendly?

/**
 * Simple proxy facet.
 *
 * @since 3.0
 */
@Named
public class SimpleProxyFacet
    extends SimpleStorageFacet
{
  public static final String CONFIG_KEY = "proxy";

  private URI remoteUrl;

  private HttpClientFacet httpClient;

  @Override
  protected void doInit() throws Exception {
    NestedAttributesMap attributes = getRepository().getConfiguration().attributes(CONFIG_KEY);
    String url = attributes.require("remoteUrl", String.class);
    if (!url.endsWith("/")) {
      url = url + "/";
    }
    this.remoteUrl = new URI(url);
    log.debug("Remote URL: {}", remoteUrl);
  }

  @Override
  protected void doStart() throws Exception {
    httpClient = getRepository().facet(HttpClientFacet.class);
  }

  @Override
  protected void doStop() throws Exception {
    httpClient = null;
  }

  @Override
  protected void doDestroy() throws Exception {
    remoteUrl = null;
  }

  @Nullable
  @Guarded(by = STARTED)
  public SimpleContent get(final String name) {
    checkNotNull(name);

    SimpleContent content = super.get(name);
    if (content == null) {
      try {
        content = fetch(name);
        if (content != null) {
          put(name, content);
        }
      }
      catch (IOException e) {
        log.warn("Failed to fetch: {}", name, e);
      }
    }

    return content;
  }

  @Nullable
  private SimpleContent fetch(final String name) throws IOException {
    HttpClient client = httpClient.getHttpClient();

    HttpGet request = new HttpGet(remoteUrl.resolve(name));
    log.debug("Fetching: {}", request);

    HttpResponse response = client.execute(request);
    log.debug("Response: {}", response);

    StatusLine status = response.getStatusLine();
    log.debug("Status: {}", status);

    SimpleContent content = null;
    if (status.getStatusCode() == HttpStatus.SC_OK) {
      HttpEntity entity = response.getEntity();
      try {
        log.debug("Entity: {}", entity);
        content = new SimpleContent(new HttpEntityPayload(response, entity));
      }
      finally {
        EntityUtils.consume(entity);
      }
    }

    return content;
  }
}
