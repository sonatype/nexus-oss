/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.component.source.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.AssetResponse;
import org.sonatype.nexus.component.source.ComponentResponse;

import com.google.common.base.Supplier;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.DateUtils;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * A helper service for building simple component responses containing a single binary asset from HttpClient responses.
 *
 * @since 3.0
 */
@Named
@Singleton
public class HttpComponentResponseBuilder
{
  public ComponentResponse simpleComponentResponse(final CloseableHttpResponse response,
                                                   final HttpEntity httpEntity,
                                                   final Map<String, Object> componentMetadata,
                                                   final Map<String, Object> assetMetadata)
      throws IOException
  {
    final AssetResponse assetResponse = new HttpAssetResponse(httpEntity.getContentLength(),
        extractContentType(httpEntity), getLastModified(response), assetMetadata,
        createStreamSupplier(response, httpEntity.getContent()));

    return new ComponentResponse()
    {
      @Override
      public Map<String, Object> getMetadata() {
        return componentMetadata;
      }

      @Override
      public Iterable<AssetResponse> getAssets() {
        return asList(assetResponse);
      }
    };
  }

  @Nullable
  private DateTime getLastModified(final HttpResponse response) {
    final Header header = response.getFirstHeader("last-modified");
    if (header != null) {
      Date date = DateUtils.parseDate(header.getValue());
      if (date != null) {
        return new DateTime(date);
      }
    }
    return null;
  }

  @Nullable
  private String extractContentType(final HttpEntity httpEntity) {
    final Header contentType = httpEntity.getContentType();
    if (contentType != null) {
      return contentType.getValue();
    }
    return null;
  }

  private Supplier<InputStream> createStreamSupplier(final CloseableHttpResponse response,
                                                     final InputStream stream)
  {
    return new Supplier<InputStream>()
    {
      @Override
      public InputStream get() {
        return new ExtraCloseableStream(checkNotNull(stream), response);
      }
    };
  }

}
