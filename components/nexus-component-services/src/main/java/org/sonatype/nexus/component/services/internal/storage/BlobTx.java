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
package org.sonatype.nexus.component.services.internal.storage;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Sets;

/**
 * Keeps track of a set of newly-created blobs and blobs that have been requested for deletion, so that they may be
 * deleted when a transaction completes.
 *
 * @since 3.0
 */
class BlobTx
    extends ComponentSupport
{
  private final BlobStore blobStore;

  private final Set<Blob> newlyCreatedBlobs = Sets.newHashSet();

  private final Set<Blob> deletionRequests = Sets.newHashSet();

  /**
   * Constructs an instance that works against a single blob store.
   *
   * TODO: Rather than using the blobStore directly here, delegate to some sort of BlobStoreManager (future CMA work)
   */
  public BlobTx(final BlobStore blobStore) {
    this.blobStore = blobStore;
  }

  /**
   * Creates a new blob, requesting that it be deleted in the event of a rollback.
   */
  public Blob create(InputStream content, Map<String, String> headers) {
    final Blob blob = blobStore.create(content, headers);
    newlyCreatedBlobs.add(blob);
    return blob;
  }

  /**
   * Requests that a blob be deleted when the transaction is committed.
   */
  public void delete(Blob blob) {
    deletionRequests.add(blob);
  }

  /**
   * Commits the transaction, deleting blobs that have been requested for deletion.
   */
  public void commit() {
    doDeletions(deletionRequests, "Unable to delete blob {} after successful transaction");
  }

  /**
   * Rolls back the transaction, deleting blobs that have been newly created.
   */
  public void rollback() {
    doDeletions(newlyCreatedBlobs, "Unable to delete blob {} after failed transaction");
  }

  private void doDeletions(final Set<Blob> blobs, String message) {
    for (Blob blob : blobs) {
      doDeleteBlob(blob, message);
    }
  }

  private void doDeleteBlob(Blob blob, String message) {
    try {
      blobStore.delete(blob.getId());
    }
    catch (Throwable t) {
      log.warn(message, t, blob.getId());
    }
  }
}
