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
package org.sonatype.nexus.repository.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.util.ExtraCloseableStream;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Status;

import com.google.common.base.Supplier;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.DateUtils;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A helper service for building {@link PayloadResponse}s from HttpClient responses.
 *
 * @since 3.0
 */
@Named
@Singleton
public class HttpPayloadResponseBuilder
{
  public PayloadResponse buildPayloadResponse(final CloseableHttpResponse response) throws IOException
  {
    final HttpEntity httpEntity = response.getEntity();

    // TODO: Convey this and other headers.
    final DateTime lastModified = getLastModified(response);

    final InputStream content = httpEntity.getContent();

    Payload payload = buildPayload(response, httpEntity, content);

    return new PayloadResponse(extractStatus(response), payload);
  }

  private Payload buildPayload(final CloseableHttpResponse response, final HttpEntity httpEntity,
                               final InputStream content)
  {
    return new Payload()
    {
      @Override
      public String getContentType() {
        return extractContentType(httpEntity);
      }

      @Override
      public long getSize() {
        return httpEntity.getContentLength();
      }

      @Override
      public InputStream openInputStream() {
        return new ExtraCloseableStream(content, response);
      }
    };
  }

  private Status extractStatus(final CloseableHttpResponse response) {
    final int statusCode = response.getStatusLine().getStatusCode();

    final boolean success = statusCode >= 200 && statusCode < 300;
    return new Status(success, statusCode);
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
