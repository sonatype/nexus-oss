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
 * Stores all the files in a single directory. An alternate implementation could compute a hash of the BlobId and
 * use that to distribute files across multiple directories.
 *
 * @since 3.0
 */
public class FlatDirectoryLocationPolicy
    implements FileLocationPolicy
{
  @Override
  public Path getHeaderPath(final Path dataDirectory, final BlobId blobId) {
    return dataDirectory.resolve(blobId.toString() + ".header");
  }

  @Override
  public Path getBlobPath(final Path dataDirectory, final BlobId blobId) {
    return dataDirectory.resolve(blobId.toString() + ".blob");
  }
}
