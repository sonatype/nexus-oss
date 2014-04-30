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
import java.nio.file.Path;
import java.util.Date;

/**
 * A wrapper around file operations to make mocking easier.
 *
 * @since 3.0
 */
public interface FileOperations
{
  /**
   * Creates a file (and its containing directories, if necessary) and populates it from the
   * InputStream, which gets closed.
   */
  void create(Path path, InputStream data) throws IOException;

  /**
   * Creates a file (and its containing directories, if necessary) and populates it from the byte array.
   */
  void create(Path path, byte[] data) throws IOException;

  boolean exists(Path path);

  InputStream openInputStream(Path path) throws IOException;

  Date fileCreationDate(Path path) throws IOException;

  String computeSha1Hash(Path path) throws IOException;

  /**
   * Returns true if the file existed before deletion, false otherwise.
   */
  boolean delete(Path path) throws IOException;

  long fileSize(Path path) throws IOException;
}
