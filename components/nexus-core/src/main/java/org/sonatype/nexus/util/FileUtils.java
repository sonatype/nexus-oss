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

package org.sonatype.nexus.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Some utils that should end in plexus-utils.
 *
 * @author cstamas
 */
public class FileUtils
{
  private static Set<File> roots = null;

  /**
   * Recursively count files in a directory.
   *
   * @return count of files in directory.
   */
  public static long filesInDirectory(final String directory)
      throws IllegalArgumentException
  {
    return filesInDirectory(new File(directory), null);
  }

  /**
   * Recursively count files in a directory.
   *
   * @return count of files in directory.
   */
  public static long filesInDirectory(final File directory, final FileFilter filter)
      throws IllegalArgumentException
  {
    if (!directory.exists()) {
      final String message = directory + " does not exist";
      throw new IllegalArgumentException(message);
    }

    if (!directory.isDirectory()) {
      final String message = directory + " is not a directory";
      throw new IllegalArgumentException(message);
    }

    return filesInDirectorySilently(directory, filter);
  }

  public static long filesInDirectorySilently(final File directory, final FileFilter filter) {
    long count = 0;

    final File[] files = directory.listFiles(filter);

    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        final File file = files[i];

        if (file.isDirectory()) {
          count += filesInDirectorySilently(file, filter);
        }
        else {
          count++;
        }
      }
    }

    return count;
  }

  /**
   * Recursively sum file sizes in a directory.
   *
   * @return count of files in directory.
   */
  public static long fileSizesInDirectory(final File directory)
      throws IllegalArgumentException
  {
    if (!directory.exists()) {
      final String message = directory + " does not exist";
      throw new IllegalArgumentException(message);
    }

    if (!directory.isDirectory()) {
      final String message = directory + " is not a directory";
      throw new IllegalArgumentException(message);
    }

    return fileSizesInDirectorySilently(directory, null);
  }

  public static long fileSizesInDirectorySilently(final File directory, final FileFilter filter) {
    long size = 0;

    final File[] files = directory.listFiles(filter);

    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        final File file = files[i];

        if (file.isDirectory()) {
          size += fileSizesInDirectorySilently(file, filter);
        }
        else {
          size += file.length();
        }
      }
    }

    return size;
  }

  public static boolean validFileUrl(String url) {
    boolean result = true;

    if (!validFile(new File(url))) {
      // Failed w/ straight file, now time to try URL
      try {
        if (!validFile(new File(new URL(url).getFile()))) {
          result = false;
        }
      }
      catch (MalformedURLException e) {
        result = false;
      }
    }

    return result;
  }

  public static boolean validFile(File file) {
    if (roots == null) {
      roots = new HashSet<File>();

      File[] listedRoots = File.listRoots();

      for (int i = 0; i < listedRoots.length; i++) {
        roots.add(listedRoots[i]);
      }

      // Allow UNC based paths on windows
      // i.e. \\someserver\repository\central\blah
      if (isWindows()) {
        roots.add(new File("\\\\"));
      }
    }

    File root = file;

    while (root.getParentFile() != null) {
      root = root.getParentFile();
    }

    return roots.contains(root);
  }

  public static boolean isWindows() {
    return System.getProperty("os.name").indexOf("Windows") != -1;
  }

  public static File getFileFromUrl(String urlPath) {
    if (validFileUrl(urlPath)) {
      try {
        URL url = new URL(urlPath);
        try {
          return new File(url.toURI());
        }
        catch (Exception t) {
          return new File(url.getPath());
        }
      }
      catch (MalformedURLException e) {
        // Try just a regular file
        return new File(urlPath);
      }
    }

    return null;
  }

  public static void move(File source, File destination)
      throws IOException
  {
    if (source == null) {
      throw new NullPointerException("source can't be null");
    }
    if (destination == null) {
      throw new NullPointerException("destination can't be null");
    }

    if (!source.exists()) {
      throw new FileNotFoundException("Source file doesn't exists " + source);
    }

    destination.getParentFile().mkdirs();
    if (!destination.exists()) {
      if (!source.renameTo(destination)) {
        throw new IOException("Failed to move '" + source + "' to '" + destination + "'");
      }
    }
    else if (source.isFile()) {
      org.codehaus.plexus.util.FileUtils.forceDelete(destination);
      if (!source.renameTo(destination)) {
        throw new IOException("Failed to move '" + source + "' to '" + destination + "'");
      }
    }
    else if (source.isDirectory()) {
      // the folder already exists the, so let's do a recursive move....
      if (destination.isFile()) {
        org.codehaus.plexus.util.FileUtils.forceDelete(destination);
        if (!source.renameTo(destination)) {
          throw new IOException("Failed to move '" + source + "' to '" + destination + "'");
        }
      }
      else {
        String[] files = source.list();
        for (String file : files) {
          move(new File(source, file), new File(destination, file));
        }

        org.codehaus.plexus.util.FileUtils.forceDelete(source);
      }
    }
  }
}
