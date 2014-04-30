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
package org.sonatype.nexus.blobstore.locking;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreMetrics;

import com.google.common.base.Preconditions;

/**
 * @since 3.0
 */
public abstract class WrappingBlobStoreSupport
    implements BlobStore
{
  private BlobStore wrappedBlobStore;

  public WrappingBlobStoreSupport(final BlobStore wrappedBlobStore) {
    Preconditions.checkNotNull(wrappedBlobStore);
    this.wrappedBlobStore = wrappedBlobStore;
  }

  @Override
  public Blob create(final InputStream inputStream, final Map<String, String> headers) {
    return wrappedBlobStore.create(inputStream, headers);
  }

  @Nullable
  @Override
  public Blob get(final BlobId blobId) {
    return wrappedBlobStore.get(blobId);
  }

  @Override
  public boolean delete(final BlobId blobId) {
    return wrappedBlobStore.delete(blobId);
  }

  @Override
  public boolean deleteHard(final BlobId blobId) {
    return wrappedBlobStore.deleteHard(blobId);
  }

  @Override
  public BlobStoreMetrics getMetrics() {
    return wrappedBlobStore.getMetrics();
  }
}
