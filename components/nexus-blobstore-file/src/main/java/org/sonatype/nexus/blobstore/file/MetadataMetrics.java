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
package org.sonatype.nexus.blobstore.file;

import org.sonatype.nexus.blobstore.api.BlobStoreMetrics;

/**
 * Aggregate metrics about the blobs in the blob store, derived from the metadata.  These are used as a component of
 * the {@link BlobStoreMetrics}, which has other information not stored in in the {@link BlobMetadataStore}.
 *
 * @since 3.0
 */
public class MetadataMetrics
{
  private final long blobCount;

  private final long totalSize;

  public MetadataMetrics(final long blobCount, final long totalSize) {
    this.blobCount = blobCount;
    this.totalSize = totalSize;
  }

  /**
   * Get an approximate count of the number of blobs in the blob store.
   */
  public long getBlobCount() {
    return blobCount;
  }

  /**
   * Get the approximate total storage space consumed by this blob store in bytes, including blobs, headers, and any
   * other metadata required by the store.
   */
  public long getTotalSize() {
    return totalSize;
  }
}
