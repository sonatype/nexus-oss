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

import javax.inject.Inject;

import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.file.utils.FilenameEscaper;

import static org.sonatype.nexus.blobstore.file.utils.FilenameEscaper.escapeFilename;

/**
 * Stores blobs and blob headers in a two-deep directory tree, the first layer having {@link #TIER_1_MODULO}
 * directories, the second having {@link #TIER_2_MODULO}.
 *
 * @since 3.0
 */
public class HashingSubdirFileLocationPolicy
    implements FilePathPolicy
{
  public static final int TIER_1_MODULO = 43;

  public static final int TIER_2_MODULO = 47;

  private Path dataDirectory;

  private FilenameEscaper escaper = new FilenameEscaper();

  @Inject
  public HashingSubdirFileLocationPolicy(final Path dataDirectory) {
    this.dataDirectory = dataDirectory;
  }

  @Override
  public Path getRoot() {
    return dataDirectory;
  }

  @Override
  public Path forContent(final BlobId blobId) {
    return dataDirectory.resolve(computeSubdirectory(blobId) + "/" + escapeFilename(blobId.getId()) + ".blob");
  }

  private String computeSubdirectory(final BlobId blobId) {
    StringBuilder subdirectory = new StringBuilder();
    subdirectory.append("vol-");
    subdirectory.append(leftPad(blobId.hashCode() % TIER_1_MODULO));

    subdirectory.append("/chap-");
    subdirectory.append(leftPad(blobId.hashCode() % TIER_2_MODULO));

    return subdirectory.toString();
  }

  private String leftPad(final int i) {
    return String.format("%02d", Math.abs(i) + 1);
  }

}
