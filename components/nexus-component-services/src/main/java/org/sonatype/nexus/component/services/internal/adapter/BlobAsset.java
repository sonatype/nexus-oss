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
package org.sonatype.nexus.component.services.internal.adapter;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.ComponentId;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link Blob}-based {@link Asset} implementation.
 *
 * @since 3.0
 */
class BlobAsset
    implements Asset
{
  private final ComponentId componentId;

  private final Blob blob;

  private final String path;

  private final String contentType;

  private final DateTime firstCreated;

  public BlobAsset(ComponentId componentId, Blob blob, @Nullable String path, @Nullable String contentType,
                   @Nullable DateTime firstCreated) {
    this.componentId = checkNotNull(componentId);
    this.blob = checkNotNull(blob);
    this.path = path;
    this.contentType = contentType;
    this.firstCreated = firstCreated;
  }

  @Override
  public ComponentId getComponentId() {
    return componentId;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public long getContentLength() {
    return blob.getMetrics().getContentSize();
  }

  @Nullable
  @Override
  public String getContentType() {
    return contentType;
  }

  @Nullable
  @Override
  public DateTime getFirstCreated() {
    return firstCreated;
  }

  @Nullable
  @Override
  public DateTime getLastModified() {
    return blob.getMetrics().getCreationTime();
  }

  @Override
  public InputStream openStream() throws IOException {
    return blob.getInputStream();
  }
}
