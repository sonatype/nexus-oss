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

import java.nio.file.Path;

import org.sonatype.nexus.blobstore.api.BlobId;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.blobstore.file.internal.FilenameEscaper.escapeFilename;

/**
 * Stores blobs and blob headers in a two-deep directory tree.
 *
 * The first layer, {@code vol}, having {@link #TIER_1_MODULO} directories,
 * and the second {@code chap} having {@link #TIER_2_MODULO}.
 *
 * @since 3.0
 */
public class HashingSubdirFileLocationPolicy
    implements FilePathPolicy
{
  private static final int TIER_1_MODULO = 43;

  private static final int TIER_2_MODULO = 47;

  private final Path root;

  public HashingSubdirFileLocationPolicy(final Path root) {
    this.root = checkNotNull(root);
  }

  @Override
  public Path getRoot() {
    return root;
  }

  @Override
  public Path forContent(final BlobId blobId) {
    checkNotNull(blobId);

    String path = String.format("vol-%02d/chap-%02d/%s.blob",
        tier(blobId, TIER_1_MODULO),
        tier(blobId, TIER_2_MODULO),
        escapeFilename(blobId.getId())
    );

    return root.resolve(path);
  }

  private int tier(final BlobId blobId, final int modulo) {
    return Math.abs(blobId.hashCode() % modulo) + 1;
  }
}
