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
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.Envelope;
import org.sonatype.nexus.component.source.ComponentRequest;
import org.sonatype.nexus.component.source.ComponentSource;
import org.sonatype.nexus.component.source.ComponentSourceId;
import org.sonatype.nexus.views.rawbinaries.internal.storage.adapter.RawBinaryAssetAdapter;
import org.sonatype.nexus.views.rawbinaries.internal.storage.adapter.RawBinaryComponentAdapter;

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_CONTENT_LENGTH;
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_CONTENT_TYPE;

/**
 * A source for remote raw binary components which provides HTTP resources under a given URL prefix as assets.
 *
 * @since 3.0
 */
public class RawBinaryComponentSource
    implements ComponentSource
{
  private final ComponentSourceId sourceName;

  private final String urlPrefix;

  private CloseableHttpClient httpClient;

  public RawBinaryComponentSource(final ComponentSourceId sourceName,
                                  final CloseableHttpClient httpClient,
                                  final String urlPrefix)
  {
    this.sourceName = checkNotNull(sourceName);
    this.urlPrefix = checkNotNull(urlPrefix);
    this.httpClient = checkNotNull(httpClient);
  }

  @Nullable
  @Override
  public Iterable<Envelope> fetchComponents(final ComponentRequest request) throws IOException
  {
    final String uri = urlPrefix + request.getQuery().get("path");

    final HttpGet httpGet = new HttpGet(uri);

    final CloseableHttpResponse response = httpClient.execute(httpGet);

    final HttpEntity httpEntity = response.getEntity();
    final Header contentType = httpEntity.getContentType();

    Asset asset = new Asset(RawBinaryAssetAdapter.CLASS_NAME);
    if (contentType != null) {
      asset.put(P_CONTENT_TYPE, contentType.getValue());
    }
    asset.put(P_CONTENT_LENGTH, 0L);
    asset.setStreamSupplier(new Supplier<InputStream>()
    {
      @Override
      public InputStream get() {
        try {
          return new ExtraCloseableStream(httpEntity.getContent(), response);
        }
        catch (IOException e) {
          throw Throwables.propagate(e);
        }
      }
    });

    Component component = new Component(RawBinaryComponentAdapter.CLASS_NAME);

    return asList(new Envelope(component, asset));
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
