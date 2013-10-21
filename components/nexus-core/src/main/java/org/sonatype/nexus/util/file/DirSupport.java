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
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Set;

import org.sonatype.sisu.goodies.common.SimpleFormat;

import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * FS directory related support class. Offers static helper methods for common FS related operations
 * used in Nexus core and plugins for manipulating FS directories (aka "folders").
 *
 * @author cstamas
 * @since 2.7.0
 */
public final class DirSupport
{
  public static final Set<FileVisitOption> DEFAULT_FILE_VISIT_OPTIONS = EnumSet.of(FileVisitOption.FOLLOW_LINKS);

  private DirSupport() {
    // no instance
  }

  // Visitors

  public static class FunctionVisitor
      extends SimpleFileVisitor<Path>
  {
    private final Function<Path, FileVisitResult> func;

    public FunctionVisitor(final Function<Path, FileVisitResult> func) {
      this.func = checkNotNull(func);
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes a) throws IOException {
      return func.apply(file);
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
      if (e != null) {
        throw e;
      }
      return func.apply(dir);
    }
  }

  public static class FunctionFileVisitor
      extends SimpleFileVisitor<Path>
  {
    private final Function<Path, FileVisitResult> func;

    public FunctionFileVisitor(final Function<Path, FileVisitResult> func) {
      this.func = checkNotNull(func);
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes a) throws IOException {
      return func.apply(file);
    }
  }

  public static class CopyVisitor
      extends SimpleFileVisitor<Path>
  {
    private final Path from;

    private final Path to;

    private final CopyOption[] copyOptions;

    public CopyVisitor(final Path from, final Path to, final CopyOption... copyOptions) {
      this.from = from;
      this.to = to;
      this.copyOptions = copyOptions;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes a) throws IOException {
      final Path targetPath = to.resolve(from.relativize(dir));
      if (!Files.exists(targetPath)) {
        Files.createDirectories(targetPath);
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes a) throws IOException {
      Files.copy(file, to.resolve(from.relativize(file)), copyOptions);
      return FileVisitResult.CONTINUE;
    }
  }

  // CLEAN: remove files recursively of a directory but keeping the directory structure intact

  public static void clean(final Path dir) throws IOException {
    validateDirectory(dir);
    Files.walkFileTree(dir, DEFAULT_FILE_VISIT_OPTIONS, Integer.MAX_VALUE,
        new SimpleFileVisitor<Path>()
        {
          @Override
          public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }
        }
    );
  }

  public static boolean cleanIfExists(final Path dir) throws IOException {
    checkNotNull(dir);
    if (Files.exists(dir)) {
      clean(dir);
      return true;
    }
    else {
      return false;
    }
  }

  // EMPTY: removes directory subtree with directory itself left intact

  public static void empty(final Path dir) throws IOException {
    validateDirectory(dir);
    Files.walkFileTree(dir, DEFAULT_FILE_VISIT_OPTIONS, Integer.MAX_VALUE,
        new SimpleFileVisitor<Path>()
        {
          @Override
          public FileVisitResult visitFile(final Path f, final BasicFileAttributes attrs) throws IOException {
            Files.delete(f);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(final Path d, final IOException exc) throws IOException {
            if (exc != null) {
              throw exc;
            }
            else if (dir != d) {
              Files.delete(d);
            }
            return FileVisitResult.CONTINUE;
          }
        }
    );
  }

  public static boolean emptyIfExists(final Path dir) throws IOException {
    checkNotNull(dir);
    if (Files.exists(dir)) {
      empty(dir);
      return true;
    }
    else {
      return false;
    }
  }

  // DELETE: removes directory subtree with directory itself recursively

  public static void delete(final Path dir) throws IOException {
    validateDirectoryOrFile(dir);
    if (Files.isDirectory(dir)) {
      Files.walkFileTree(dir, DEFAULT_FILE_VISIT_OPTIONS, Integer.MAX_VALUE,
          new SimpleFileVisitor<Path>()
          {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
              Files.delete(file);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
              if (exc != null) {
                throw exc;
              }
              else {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
              }
            }
          }
      );
    }
    else {
      Files.delete(dir);
    }
  }

  public static boolean deleteIfExists(final Path dir) throws IOException {
    checkNotNull(dir);
    if (Files.exists(dir)) {
      delete(dir);
      return true;
    }
    else {
      return false;
    }
  }

  // COPY: recursive copy of whole directory tree

  public static void copy(final Path from, final Path to) throws IOException {
    copy(from, to, StandardCopyOption.REPLACE_EXISTING);
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
    validateDirectoryOrFile(from);
    checkNotNull(to);
    if (Files.isDirectory(from)) {
      Files.walkFileTree(from, DEFAULT_FILE_VISIT_OPTIONS, Integer.MAX_VALUE,
          new CopyVisitor(from, to, options));
    }
    else {
      Files.createDirectories(to.getParent());
      Files.copy(from, to, options);
    }
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

  // MOVE: recursive copy of whole directory tree and then deleting it

  public static void move(final Path from, final Path to) throws IOException {
    copy(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    delete(from);
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

  // APPLY: applies a function to dir tree, the function should not have any IO "side effect"

  public static void apply(final Path from, final Function<Path, FileVisitResult> func) throws IOException {
    validateDirectory(from);
    Files.walkFileTree(from, DEFAULT_FILE_VISIT_OPTIONS, Integer.MAX_VALUE, new FunctionVisitor(func));
  }

  public static void applyToFiles(final Path from, final Function<Path, FileVisitResult> func) throws IOException {
    validateDirectory(from);
    Files.walkFileTree(from, DEFAULT_FILE_VISIT_OPTIONS, Integer.MAX_VALUE, new FunctionFileVisitor(func));
  }

  // Validation

  private static void validateDirectory(final Path... paths) {
    for (Path path : paths) {
      checkNotNull(path, "Path must be non-null");
      checkArgument(Files.isDirectory(path), SimpleFormat.template("%s is not a directory", path));
    }
  }

  private static void validateDirectoryOrFile(final Path... paths) {
    for (Path path : paths) {
      checkNotNull(path, "Path must be non-null");
      checkArgument(Files.exists(path), SimpleFormat.template("%s does not exists", path));
    }
  }

}
