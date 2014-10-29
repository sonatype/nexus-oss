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
package org.sonatype.nexus.component.model;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;
import org.joda.time.DateTime;

/**
 * Concrete base implementation of {@link Asset}.
 *
 * @since 3.0
 */
public class BaseAsset
    extends BaseEntity
    implements Asset
{
  private EntityId componentId;

  private long contentLength = -1;

  private String contentType;

  private DateTime firstCreated;

  private DateTime lastModified;

  private Supplier<InputStream> streamSupplier;

  @Nullable
  @Override
  public EntityId getComponentId() {
    return componentId;
  }

  @Override
  public void setComponentId(final EntityId componentId) {
    this.componentId = componentId;
  }

  @Override
  public long getContentLength() {
    return contentLength;
  }

  @Override
  public void setContentLength(final long contentLength) {
    this.contentLength = contentLength;
  }

  @Nullable
  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public void setContentType(final String contentType) {
    this.contentType = contentType;
  }

  @Nullable
  @Override
  public DateTime getFirstCreated() {
    return firstCreated;
  }

  @Override
  public void setFirstCreated(final DateTime firstCreated) {
    this.firstCreated = firstCreated;
  }

  @Nullable
  @Override
  public DateTime getLastModified() {
    return lastModified;
  }

  @Override
  public void setLastModified(final DateTime lastModified) {
    this.lastModified = lastModified;
  }

  @Nullable
  @Override
  public InputStream openStream() throws IOException {
    if (streamSupplier == null) {
      return null;
    }
    else {
      return streamSupplier.get();
    }
  }

  @Override
  public void setStreamSupplier(final Supplier<InputStream> streamSupplier) {
    this.streamSupplier = streamSupplier;
  }
}
