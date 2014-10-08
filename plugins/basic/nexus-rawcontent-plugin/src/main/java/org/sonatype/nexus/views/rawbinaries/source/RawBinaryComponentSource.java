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
import java.io.InputStream;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.source.api.ComponentEnvelope;
import org.sonatype.nexus.component.source.api.ComponentRequest;
import org.sonatype.nexus.component.source.api.ComponentSourceId;
import org.sonatype.nexus.component.source.api.PullComponentSource;
import org.sonatype.nexus.views.rawbinaries.internal.RawComponent;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * A source for remote {@link RawComponent}s which looks them up based on a URL prefix, appending the
 *
 * @since 3.0
 */
public class RawBinaryComponentSource
    implements PullComponentSource
{
  private final ComponentSourceId sourceName;

  private final String urlPrefix;

  public RawBinaryComponentSource(final ComponentSourceId sourceName, final String urlPrefix) {
    this.sourceName = checkNotNull(sourceName);
    this.urlPrefix = checkNotNull(urlPrefix);
  }

  @Nullable
  @Override
  public Iterable<ComponentEnvelope<RawComponent>> fetchComponents(final ComponentRequest request)
      throws IOException
  {
    CloseableHttpClient httpclient = HttpClients.createDefault();

    // TODO: A bit of validation here

    final String uri = urlPrefix + request.getQuery().get("path");

    HttpGet httpGet = new HttpGet(uri);

    try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
      System.out.println(response.getStatusLine());
      final HttpEntity httpEntity = response.getEntity();

      final Asset asset = createAsset(httpEntity);

      return asList(ComponentEnvelope.simpleEnvelope(new RawComponent(), asset));
    }
  }

  private Asset createAsset(final HttpEntity entity1) {
    return new Asset()
    {
      @Override
      public String getPath() {
        return ".";
      }

      @Override
      public long getContentLength() {
        return 0;
      }

      @Nullable
      @Override
      public String getContentType() {
        return entity1.getContentType().getValue();
      }

      @Nullable
      @Override
      public DateTime getFirstCreated() {
        return null;
      }

      @Nullable
      @Override
      public DateTime getLastModified() {
        return null;
      }

      @Override
      public InputStream openStream() throws IOException {
        return entity1.getContent();
      }
    };
  }

  @Override
  public ComponentSourceId getId() {
    return sourceName;
  }

  @Override
  public boolean isAvailable() {
    return true;
  }
}
