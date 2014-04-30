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
package org.sonatype.nexus.blobstore.mirror;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreException;
import org.sonatype.nexus.blobstore.api.BlobStoreMetrics;
import org.sonatype.nexus.blobstore.mirror.streams.SpoolingStreamCopier;
import org.sonatype.nexus.blobstore.mirror.streams.StreamCopier;
import org.sonatype.nexus.blobstore.mirror.streams.TempFileSpool;
import org.sonatype.nexus.blobstore.locking.BlobLockFactory;

import com.google.common.base.Preconditions;

/**
 * A blob store that reads and writes from a primary store, while mirroring writes to a backing store. The blobs in the
 * primary store have an additional header that indicates the BlobId of the blob in the mirror store.  The assumption
 * is that the mirrored store is more durable than the primary store.
 *
 * NOTE: This is a prototype, and will consume gobs of memory if large blobs are sent in.
 *
 * @since 3.0
 */
public class MirroredBlobStore
    implements BlobStore
{
  private final String blobStoreId;

  private BlobStore primaryStore;

  private BlobStore mirrorStore;

  private BlobLockFactory lockProvider;

  public MirroredBlobStore(final String blobStoreId, final BlobStore primaryStore, final BlobStore mirrorStore) {
    this.blobStoreId = blobStoreId;
    Preconditions.checkNotNull(primaryStore);
    Preconditions.checkNotNull(mirrorStore);
    this.primaryStore = primaryStore;
    this.mirrorStore = mirrorStore;
  }

  @Override
  public Blob create(final InputStream inputStream, final Map<String, String> headers) {
    try {
      final StreamCopier copier = new SpoolingStreamCopier(inputStream, new TempFileSpool());

      final Blob mirroredBlob = mirrorStore.create(copier.getOriginalStream(), headers);

      final HashMap<String, String> primaryHeaders = new HashMap<>(headers);
      primaryHeaders.put(getSecondaryBlobIdKey(), mirroredBlob.getId().toString());

      return primaryStore.create(copier.getCopiedStream(), primaryHeaders);
    }
    catch (IOException e) {
      throw new BlobStoreException(e);
    }
  }

  @Nullable
  @Override
  public Blob get(final BlobId blobId) {
    return primaryStore.get(blobId);
  }

  @Override
  public boolean delete(final BlobId blobId) {
    final Blob blob = primaryStore.get(blobId);

    final String mirrorBlobId = blob.getHeaders().get(getSecondaryBlobIdKey());
    primaryStore.delete(blobId);
    return mirrorStore.delete(new BlobId(mirrorBlobId));
  }

  @Override
  public boolean deleteHard(final BlobId blobId) {
    final Blob blob = primaryStore.get(blobId);

    final String mirrorBlobId = blob.getHeaders().get(getSecondaryBlobIdKey());
    primaryStore.deleteHard(blobId);
    return mirrorStore.deleteHard(new BlobId(mirrorBlobId));
  }

  private String getSecondaryBlobIdKey() {
    return "blobId-in-store:" + blobStoreId;
  }

  @Override
  public BlobStoreMetrics getMetrics() {
    return primaryStore.getMetrics();
  }

  public void setLockProvider(final BlobLockFactory lockProvider) {
    this.lockProvider = lockProvider;
  }
}
