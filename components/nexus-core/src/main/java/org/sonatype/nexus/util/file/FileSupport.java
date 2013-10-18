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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.sonatype.sisu.goodies.common.SimpleFormat;

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

  // Validation

  private static void validateFile(final Path... paths) {
    for (Path path : paths) {
      checkNotNull(path, "Path must be non-null");
      checkArgument(Files.isRegularFile(path), SimpleFormat.template("%s is not a regular file", path));
    }
  }

}
