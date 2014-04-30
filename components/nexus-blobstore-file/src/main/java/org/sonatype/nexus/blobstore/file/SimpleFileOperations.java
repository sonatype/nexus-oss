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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.sonatype.nexus.util.DigesterUtils;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.0
 */
public class SimpleFileOperations
    implements FileOperations
{

  private static final Logger logger = LoggerFactory.getLogger(FileBlobStore.class);

  @Override
  public void create(final Path path, final InputStream data) throws IOException {
    ensureDirectoryExists(path.getParent());

    try (final OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
      ByteStreams.copy(data, outputStream);
      data.close();
    }
  }

  @Override
  public void create(final Path path, final byte[] data) throws IOException {
    ensureDirectoryExists(path.getParent());
    Files.write(path, data, StandardOpenOption.CREATE_NEW);
  }

  @Override
  public boolean exists(final Path path) {
    return Files.exists(path);
  }

  @Override
  public InputStream openInputStream(final Path path) throws IOException {
    return Files.newInputStream(path, StandardOpenOption.READ);
  }

  @Override
  public Date fileCreationDate(final Path path) throws IOException {
    BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
    return new Date(attr.creationTime().toMillis());
  }

  @Override
  public String computeSha1Hash(final Path path) throws IOException {
    try (InputStream inputStream = openInputStream(path)) {
      return DigesterUtils.getSha1Digest(inputStream);
    }
  }

  @Override
  public boolean delete(final Path path) throws IOException {
      return Files.deleteIfExists(path);
  }

  @Override
  public long fileSize(final Path path) throws IOException {
    return Files.size(path);
  }

  /**
   * Creates directories if they don't exist.  This method is synchronized as a simple way of ensuring that multiple
   * threads aren't fighting to create the same directories at the same time.
   */
  private synchronized void ensureDirectoryExists(final Path directory) throws IOException {
    if (!exists(directory)) {
      Files.createDirectories(directory);
    }
  }
}
