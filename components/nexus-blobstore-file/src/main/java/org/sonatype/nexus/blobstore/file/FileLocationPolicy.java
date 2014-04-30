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

/**
 * Indicates where a blob with a given blob ID should be stored, relative to a blob store root. Implementations might
 * use algorithms to divide files up into multiple directories for easier management.
 *
 * @since 3.0
 */
public interface FileLocationPolicy
{
  /**
   * Returns a path to a file where the blob's content should be stored.
   */
  Path getBlobPath(Path dataDirectory, BlobId blobId);

  /**
   * Returns a path to a file where the blob's headers are stored.
   */
  Path getHeaderPath(Path dataDirectory, BlobId blobId);
}
