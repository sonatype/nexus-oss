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
package org.sonatype.nexus.component.source.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.source.AssetResponse;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A basic implementation of {@link AssetResponse} for binary assets returned via HTTP.
 *
 * @since 3.0
 */
public class HttpAssetResponse
    implements AssetResponse
{
  private final long contentLength;

  private final String contentType;

  private final DateTime lastModified;

  private final Map<String, Object> metadata;

  private final Supplier<InputStream> streamSupplier;

  public HttpAssetResponse(final long contentLength, final String contentType, final DateTime lastModified,
                           final Map<String, Object> metadata,
                           final Supplier<InputStream> streamSupplier)
  {
    this.contentLength = contentLength;
    this.contentType = contentType;
    this.lastModified = lastModified;
    this.metadata = ImmutableMap.copyOf(checkNotNull(metadata));
    this.streamSupplier = checkNotNull(streamSupplier);
  }

  @Override
  public long getContentLength() {
    return contentLength;
  }

  @Nullable
  @Override
  public String getContentType() {
    return contentType;
  }

  @Nullable
  @Override
  public DateTime getLastModified() {
    return lastModified;
  }

  @Override
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  @Override
  public InputStream openStream() throws IOException {
    return streamSupplier.get();
  }
}
