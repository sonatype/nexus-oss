/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.blobstore.file;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.common.collect.AutoClosableIterable;
import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

/**
 * @since 3.0
 */
public interface BlobMetadataStore
    extends Lifecycle
{
  /**
   * Adds the metadata, and returns the key it's now associated with.
   */
  BlobId add(BlobMetadata metadata);

  @Nullable
  BlobMetadata get(BlobId key);

  void update(BlobId blobId, BlobMetadata metadata);

  void delete(BlobId blobId);

  /**
   * Returns iterable with all blob-ids in the given state.
   *
   * @return Iterable handle must be closed when finished using it.
   */
  AutoClosableIterable<BlobId> findWithState(BlobState state);

  long getBlobCount();

  long getTotalSize();

  void compact();
}
