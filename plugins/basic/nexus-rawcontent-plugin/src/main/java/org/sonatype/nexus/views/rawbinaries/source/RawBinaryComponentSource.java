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
package org.sonatype.nexus.views.rawbinaries.source;

import java.io.IOException;
import java.util.HashMap;

import org.sonatype.nexus.component.source.ComponentRequest;
import org.sonatype.nexus.component.source.ComponentResponse;
import org.sonatype.nexus.component.source.ComponentSource;
import org.sonatype.nexus.component.source.ComponentSourceId;
import org.sonatype.nexus.component.source.support.HttpComponentResponseBuilder;
import org.sonatype.nexus.views.rawbinaries.internal.RawComponent;

import com.google.common.collect.Maps;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A source for remote {@link RawComponent}s which provides HTTP resources under a given URL prefix as assets.
 *
 * @since 3.0
 */
public class RawBinaryComponentSource
    implements ComponentSource
{
  private final ComponentSourceId sourceName;

  private final String urlPrefix;

  private final HttpComponentResponseBuilder responseBuilder;

  private CloseableHttpClient httpClient;

  public RawBinaryComponentSource(final ComponentSourceId sourceName,
                                  final CloseableHttpClient httpClient,
                                  final String urlPrefix, final HttpComponentResponseBuilder responseBuilder)
  {
    this.sourceName = checkNotNull(sourceName);
    this.urlPrefix = checkNotNull(urlPrefix);
    this.httpClient = checkNotNull(httpClient);
    this.responseBuilder = checkNotNull(responseBuilder);
  }

  @Override
  public ComponentResponse fetchComponents(final ComponentRequest request) throws IOException {
    final String uri = urlPrefix + request.getQuery().get("path");

    final HttpGet httpGet = new HttpGet(uri);

    final CloseableHttpResponse response = httpClient.execute(httpGet);

    final HttpEntity httpEntity = response.getEntity();

    final HashMap<String, Object> componentHashMap = Maps.newHashMap();
    final HashMap<String, Object> assetHashMap = Maps.newHashMap();
    return responseBuilder.simpleComponentResponse(response, httpEntity, componentHashMap, assetHashMap);
  }

  @Override
  public ComponentSourceId getId() {
    return sourceName;
  }
}
