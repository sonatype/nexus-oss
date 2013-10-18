/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.util.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.sonatype.sisu.goodies.common.SimpleFormat;

import com.google.common.base.Charsets;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * FS regular file related support class. Offers static helper methods for common FS related operations
 * used in Nexus core and plugins for manipulating FS files (aka "regular files").
 *
 * @author cstamas
 * @since 2.7.0
 */
public final class FileSupport
{
  public static final Charset DEFAULT_CHARSET = Charsets.UTF_8;

  public static final CopyOption[] DEFAULT_COPY_OPTIONS = {
      StandardCopyOption.REPLACE_EXISTING
  };

  public static final CopyOption[] DEFAULT_MOVE_OPTIONS = {
      StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES
  };

  private FileSupport() {
    // no instance
  }

  // DELETE

  public static void delete(final Path file) throws IOException {
    validateFile(file);
    Files.delete(file);
  }

  public static boolean deleteIfExists(final Path file) throws IOException {
    checkNotNull(file);
    return Files.deleteIfExists(file);
  }

  // COPY: file to file

  public static void copy(final Path from, final Path to) throws IOException {
    // "copy": overwrite if exists + make files appear as "new" + copy as link if link
    copy(from, to, DEFAULT_COPY_OPTIONS);
  }

  public static boolean copyIfExists(final Path from, final Path to) throws IOException {
    checkNotNull(from);
    if (Files.exists(from)) {
      copy(from, to);
      return true;
    }
    else {
      return false;
    }
  }

  public static void copy(final Path from, final Path to, final CopyOption... options) throws IOException {
    validateFile(from);
    checkNotNull(to);
    Files.copy(from, to, options);
  }

  public static boolean copyIfExists(final Path from, final Path to, final CopyOption... options) throws IOException {
    checkNotNull(from);
    if (Files.exists(from)) {
      copy(from, to, options);
      return true;
    }
    else {
      return false;
    }
  }

  // COPY: stream to file

  public static void copy(final InputStream from, final Path to) throws IOException {
    // "copy": overwrite if exists + make files appear as "new" + copy as link if link
    copy(from, to, DEFAULT_COPY_OPTIONS);
  }

  public static void copy(final InputStream from, final Path to, final CopyOption... options) throws IOException {
    checkNotNull(to);
    Files.copy(from, to, options);
  }

  // MOVE: file to file

  public static void move(final Path from, final Path to) throws IOException {
    // "move": overwrite if exists + make files appear as "new" + copy as link if link
    move(from, to, DEFAULT_MOVE_OPTIONS);
  }

  public static boolean moveIfExists(final Path from, final Path to) throws IOException {
    checkNotNull(from);
    if (Files.exists(from)) {
      move(from, to);
      return true;
    }
    else {
      return false;
    }
  }

  public static void move(final Path from, final Path to, final CopyOption... options) throws IOException {
    validateFile(from);
    checkNotNull(to);
    Files.move(from, to, options);
  }

  public static boolean moveIfExists(final Path from, final Path to, final CopyOption... options) throws IOException {
    checkNotNull(from);
    if (Files.exists(from)) {
      move(from, to, options);
      return true;
    }
    else {
      return false;
    }
  }

  // READ: reading them up as String

  public static String readFile(final Path file)
      throws IOException
  {
    return readFile(file, DEFAULT_CHARSET);
  }

  public static String readFile(final Path file, final Charset charset)
      throws IOException
  {
    checkNotNull(file);
    checkNotNull(charset);
    try (final BufferedReader reader = Files.newBufferedReader(file, charset)) {
      final StringBuilder result = new StringBuilder();
      while (true) {
        final String line = reader.readLine();
        if (line == null) {
          break;
        }
        result.append(line).append("\n");
      }
      return result.toString();
    }
  }

  public static void writeFile(final Path file, final String payload)
      throws IOException
  {
    writeFile(file, DEFAULT_CHARSET, payload);
  }

  public static void writeFile(final Path file, final Charset charset, final String payload)
      throws IOException
  {
    checkNotNull(file);
    checkNotNull(charset);
    try (final BufferedWriter writer = Files.newBufferedWriter(file, charset)) {
      writer.write(payload);
      writer.flush();
    }
  }


  // Validation

  private static void validateFile(final Path... paths) {
    for (Path path : paths) {
      checkNotNull(path, "Path must be non-null");
      checkArgument(Files.isRegularFile(path), SimpleFormat.template("%s is not a regular file", path));
    }
  }

}
