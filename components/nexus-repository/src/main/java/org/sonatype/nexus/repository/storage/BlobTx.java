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
package org.sonatype.nexus.repository.storage;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Keeps track of added and to-be-deleted blobs so they can be deleted as appropriate when the transaction ends,
 * via commit or rollback.
 *
 * @since 3.0
 */
class BlobTx
  extends ComponentSupport
{
  private final BlobStore blobStore;

  private final Set<BlobRef> newlyCreatedBlobs = Sets.newHashSet();

  private final Set<BlobRef> deletionRequests = Sets.newHashSet();

  public BlobTx(final BlobStore blobStore) {
    this.blobStore = checkNotNull(blobStore);
  }

  public BlobRef create(InputStream inputStream, Map<String, String> headers) {
    Blob blob = blobStore.create(inputStream, headers);
    BlobRef blobRef = new BlobRef("NODE", "STORE", blob.getId().asUniqueString());
    newlyCreatedBlobs.add(blobRef);
    return blobRef;
  }

  @Nullable
  public Blob get(BlobRef blobRef) {
    return blobStore.get(blobRef.getBlobId());
  }

  public void delete(BlobRef blobRef) {
    deletionRequests.add(blobRef);
  }

  public void commit() {
    doDeletions(deletionRequests, "Unable to delete old blob {} while committing transaction");
    clearState();
  }

  public void rollback() {
    doDeletions(newlyCreatedBlobs, "Unable to delete new blob {} while rolling back transaction");
    clearState();
  }

  private void doDeletions(Set<BlobRef> blobRefs, String failureMessage) {
    for (BlobRef blobRef : blobRefs) {
      try {
        blobStore.delete(blobRef.getBlobId());
      }
      catch (Throwable t) {
        log.warn(failureMessage, t, blobRef);
      }
    }
  }

  private void clearState() {
    newlyCreatedBlobs.clear();
    deletionRequests.clear();
  }
}
