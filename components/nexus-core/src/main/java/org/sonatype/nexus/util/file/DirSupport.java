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

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.EnumSet;
import java.util.Set;

import org.sonatype.sisu.goodies.common.SimpleFormat;

import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * FS directory related support class. Offers static helper methods for common FS related operations
 * used in Nexus core and plugins for manipulating FS directories (aka "folders"). Goal of this class is
 * to utilize new Java7 NIO Files and related classes for better error detection.
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

    public CopyVisitor(final Path from, final Path to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes a) throws IOException {
      final Path targetPath = to.resolve(from.relativize(dir));
      if (!Files.exists(targetPath)) {
        mkdir(targetPath);
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes a) throws IOException {
      Files.copy(file, to.resolve(from.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
      return FileVisitResult.CONTINUE;
    }
  }

  // MKDIR: directory creation resilient to symlinks

  /**
   * Creates a directory. Fails only if directory creation fails, otherwise cleanly returns. If cleanly returns,
   * it is guaranteed that passed in path is created (with all parents as needed) successfully. Unlike Java7
   * {@link Files#createDirectories(Path, FileAttribute[])} method, this method does support paths having last
   * path element a symlink too. In this case, it's verified that symlink points to a directory and is readable.
   */
  public static void mkdir(final Path dir) throws IOException {
    try {
      Files.createDirectories(dir);
    }
    catch (FileAlreadyExistsException e) {
      // this happens when last element of path exists, but is a symlink.
      // A simple test with Files.isDirectory should be able to  detect this
      // case as by default, it follows symlinks.
      if (!Files.isDirectory(dir)) {
        throw e;
      }
    }
  }

  /**
   * @since 2.8
   */
  public static void mkdir(final File dir) throws IOException {
    mkdir(dir.toPath());
  }

  // CLEAN: remove files recursively of a directory but keeping the directory structure intact

  /**
   * Cleans an existing directory from any (non-directory) files recursively. Accepts only existing
   * directories, and when returns, it's guaranteed that this directory might contain only subdirectories
   * that also might contain only subdirectories (recursively).
   */
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

  /**
   * Invokes {@link #clean(Path)} if passed in path exists and is a directory. Also, in that case {@code true} is
   * returned, and in any other case (path does not exists) {@code false} is returned.
   */
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

  /**
   * Empties an existing directory, by recursively removing all it's siblings, regular files and directories. Accepts
   * only existing directories, and when returns, it's guaranteed that passed in directory will be emptied (will
   * have no siblings, not counting OS specific ones, will be "empty" as per current OS is empty defined).
   */
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

  /**
   * Invokes {@link #empty(Path)} if passed in path exists and is a directory. Also, in that case {@code true} is
   * returned, and in any other case (path does not exists) {@code false} is returned.
   */
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

  /**
   * Deletes a file or directory recursively. This method accepts paths denoting regular files and directories. In case
   * of directory, this method will recursively delete all of it siblings and the passed in directory too.
   */
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

  /**
   * Invokes {@link #delete(Path)} if passed in path exists. Also, in that case {@code true} is
   * returned, and in any other case (path does not exists) {@code false} is returned.
   */
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

  /**
   * Copies path "from" to path "to". This method accepts both existing regular files and existing directories. If
   * "from" is a directory, a recursive copy happens of the whole subtree with "from" directory as root. Caller may
   * alter behaviour of Copy operation using copy options, as seen on {@link Files#copy(Path, Path, CopyOption...)}.
   */
  public static void copy(final Path from, final Path to) throws IOException {
    validateDirectoryOrFile(from);
    checkNotNull(to);
    if (Files.isDirectory(from)) {
      Files.walkFileTree(from, DEFAULT_FILE_VISIT_OPTIONS, Integer.MAX_VALUE,
          new CopyVisitor(from, to));
    }
    else {
      mkdir(to.getParent());
      Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Invokes {@link #copy(Path, Path)} if passed in "from" path exists and returns {@code true}. If
   * "from" path does not exists, {@code false} is returned.
   */
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

  // MOVE: recursive copy of whole directory tree and then deleting it

  /**
   * Performs a move operation, but as a sequence of "copy" and then "delete" (not a real move!). This method accepts
   * existing Paths that might denote a regular file or a directory.
   */
  public static void move(final Path from, final Path to) throws IOException {
    copy(from, to);
    delete(from);
  }

  /**
   * Invokes {@link #move(Path, Path)} if passed in "from" path exists and returns {@code true}. If
   * "from" path does not exists, {@code false} is returned.
   */
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

  /**
   * Traverses the subtree starting with "from" and applies passed in {@link Function} onto files and directories.
   * This method accepts only existing directories.
   */
  public static void apply(final Path from, final Function<Path, FileVisitResult> func) throws IOException {
    validateDirectory(from);
    Files.walkFileTree(from, DEFAULT_FILE_VISIT_OPTIONS, Integer.MAX_VALUE, new FunctionVisitor(func));
  }

  /**
   * Traverses the subtree starting with "from" and applies passed in {@link Function} onto files only.
   * This method accepts only existing directories.
   */
  public static void applyToFiles(final Path from, final Function<Path, FileVisitResult> func) throws IOException {
    validateDirectory(from);
    Files.walkFileTree(from, DEFAULT_FILE_VISIT_OPTIONS, Integer.MAX_VALUE, new FunctionFileVisitor(func));
  }

  // Validation

  /**
   * Enforce all passed in paths are non-null and is existing directory.
   */
  private static void validateDirectory(final Path... paths) {
    for (Path path : paths) {
      checkNotNull(path, "Path must be non-null");
      checkArgument(Files.isDirectory(path), SimpleFormat.template("%s is not a directory", path));
    }
  }

  /**
   * Enforce all passed in paths are non-null and exist.
   */
  private static void validateDirectoryOrFile(final Path... paths) {
    for (Path path : paths) {
      checkNotNull(path, "Path must be non-null");
      checkArgument(Files.exists(path), SimpleFormat.template("%s does not exists", path));
    }
  }

}
