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
package org.sonatype.nexus.repository.httpbridge.internal;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.repository.view.Payload;

import org.joda.time.DateTime;

/**
 * HTTP request payload adapts {@link HttpServletRequest} body-content to {@link Payload}.
 *
 * @since 3.0
 */
public class HttpRequestPayload
    implements Payload
{
  private final HttpServletRequest request;

  private final String contentType;

  private final long size;

  private final DateTime lastModified;

  public HttpRequestPayload(final HttpServletRequest request) {
    this.request = request;
    this.contentType = request.getContentType();
    this.size = request.getContentLength();

    final long lastModifiedHeader = request.getDateHeader("Last-Modified");
    this.lastModified = lastModifiedHeader == -1 ? null : new DateTime(lastModifiedHeader);
  }

  @Nullable
  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public long getSize() {
    return size;
  }

  @Nullable
  @Override
  public DateTime getLastModified() {
    return lastModified;
  }

  @Override
  public InputStream openInputStream() throws IOException {
    return request.getInputStream();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "contentType='" + contentType + '\'' +
        ", size=" + size +
        ", lastModified=" + lastModified +
        '}';
  }
}
