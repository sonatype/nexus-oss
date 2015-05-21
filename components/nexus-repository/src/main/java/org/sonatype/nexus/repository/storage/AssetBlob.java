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

import java.util.Map;

import javax.annotation.Nonnull;

import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.common.hash.HashAlgorithm;

import com.google.common.hash.HashCode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Blob handle, that holds properties of newly created Blob that is about to be assigned to an {@link Asset}.
 *
 * @since 3.0
 */
public class AssetBlob
{
  private final BlobRef blobRef;

  private final long size;

  private final String contentType;

  private final Map<HashAlgorithm, HashCode> hashes;

  private boolean attached;

  public AssetBlob(final BlobRef blobRef,
                   final long size,
                   final String contentType,
                   final Map<HashAlgorithm, HashCode> hashes)
  {
    this.blobRef = checkNotNull(blobRef);
    this.size = size;
    this.contentType = checkNotNull(contentType);
    this.hashes = checkNotNull(hashes);
    this.attached = false;
  }

  boolean isAttached() {
    return attached;
  }

  void setAttached(final boolean attached) {
    this.attached = attached;
  }

  @Nonnull
  public BlobRef getBlobRef() {
    return blobRef;
  }

  public long getSize() {
    return size;
  }

  @Nonnull
  public String getContentType() {
    return contentType;
  }

  @Nonnull
  public Map<HashAlgorithm, HashCode> getHashes() {
    return hashes;
  }
}