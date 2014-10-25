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

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.ComponentId;
import org.sonatype.nexus.component.source.api.ComponentEnvelope;
import org.sonatype.nexus.component.source.api.ComponentRequest;
import org.sonatype.nexus.component.source.api.ComponentSourceId;
import org.sonatype.nexus.component.source.api.PullComponentSource;
import org.sonatype.nexus.views.rawbinaries.internal.RawComponent;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * A source for remote {@link RawComponent}s which provides HTTP resources under a given URL prefix as assets.
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
    final CloseableHttpClient httpclient = HttpClients.createDefault();

    final String uri = urlPrefix + request.getQuery().get("path");

    final HttpGet httpGet = new HttpGet(uri);

    final CloseableHttpResponse response = httpclient.execute(httpGet);

    return asList(ComponentEnvelope.simpleEnvelope(new RawComponent(), new RequestClosingAsset(response)));
  }

  /**
   * An asset implementation that will take care of closing the underlying CloseableHttpResponse that backs the
   * input stream from the remote source.
   */
  private static class RequestClosingAsset
      implements Asset
  {
    final CloseableHttpResponse response;

    final HttpEntity entity;

    private RequestClosingAsset(final CloseableHttpResponse response) {
      this.response = response;
      this.entity = response.getEntity();
    }

    @Override
    public ComponentId getComponentId() {
      return null;
    }

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
      final Header contentType = entity.getContentType();
      return contentType == null ? null : contentType.getValue();
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
      return new ExtraCloseableStream(entity.getContent(), response);
    }
  }

  /**
   * A way of attaching a Closeable to an input stream, so the auto is closed as soon as the stream is.
   */
  private static class ExtraCloseableStream
      extends FilterInputStream
  {
    final Closeable needsClosing;

    private ExtraCloseableStream(final InputStream in, final Closeable needsClosing) {
      super(in);
      this.needsClosing = needsClosing;
    }

    @Override
    public void close() throws IOException {
      super.close();
      IOUtils.closeQuietly(needsClosing);
    }

  }

  @Override
  public ComponentSourceId getId() {
    return sourceName;
  }
}
