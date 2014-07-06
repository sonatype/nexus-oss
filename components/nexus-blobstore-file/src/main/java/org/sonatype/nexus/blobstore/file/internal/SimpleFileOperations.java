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

package org.sonatype.nexus.blobstore.file.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.blobstore.file.FileOperations;
import org.sonatype.nexus.util.file.DirSupport;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.io.ByteStreams;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple {@code java.nio} implementation of {@link FileOperations}.
 *
 * @since 3.0
 */
@Named("simple")
@Singleton
public class SimpleFileOperations
    extends ComponentSupport
    implements FileOperations
{
  @Override
  public StreamMetrics create(final Path path, final InputStream data) throws IOException {
    checkNotNull(path);
    checkNotNull(data);
    ensureDirectoryExists(path.getParent());

    final MetricsInputStream input = new MetricsInputStream(data, DigestHelper.createSha1());
    try {
      try (final OutputStream output = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
        ByteStreams.copy(input, output);
      }
    }
    finally {
      data.close();
    }

    return input.getMetrics();
  }

  @Override
  public boolean exists(final Path path) {
    checkNotNull(path);
    return Files.exists(path);
  }

  @Override
  public InputStream openInputStream(final Path path) throws IOException {
    checkNotNull(path);
    return Files.newInputStream(path, StandardOpenOption.READ);
  }

  @Override
  public boolean delete(final Path path) throws IOException {
    checkNotNull(path);
    try {
      return Files.deleteIfExists(path);
    }
    finally {
      // FIXME: This is error prone to throw in finally block
      if (exists(path)) {
        throw new IOException("File was not successfully deleted.");
      }
    }
  }

  /**
   * Creates directories if they don't exist.  This method is synchronized as a simple way of ensuring that multiple
   * threads aren't fighting to create the same directories at the same time.
   */
  private synchronized void ensureDirectoryExists(final Path directory) throws IOException {
    checkNotNull(directory);
    if (!exists(directory)) {
      DirSupport.mkdir(directory);
    }
  }
}
