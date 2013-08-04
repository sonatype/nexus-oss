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

package org.sonatype.nexus.proxy.wastebasket;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.scheduling.TaskUtil;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRepositoryFolderCleaner
    implements RepositoryFolderCleaner
{
  public static final String GLOBAL_TRASH_KEY = "trash";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Requirement
  private ApplicationConfiguration applicationConfiguration;

  protected Logger getLogger() {
    return logger;
  }

  protected ApplicationConfiguration getApplicationConfiguration() {
    return applicationConfiguration;
  }

  /**
   * Delete the file forever, or just keep it by renaming it (hence, will not be used anymore).
   *
   * @param file          file to be deleted
   * @param deleteForever if it's true, delete the file forever, if it's false, move the file to trash
   */
  protected void delete(final File file, final boolean deleteForever)
      throws IOException
  {
    if (!deleteForever) {
      File basketFile =
          new File(getApplicationConfiguration().getWorkingDirectory(GLOBAL_TRASH_KEY), file.getName());

      if (file.isDirectory()) {
        FileUtils.mkdir(basketFile.getAbsolutePath());

        FileUtils.copyDirectoryStructure(file, basketFile);
      }
      else {
        FileUtils.copyFile(file, basketFile);
      }
    }
    if (file.isDirectory()) {
      deleteFilesRecursively(file);
    }
    else {
      FileUtils.forceDelete(file);
    }
  }

  // This method prevents locked files on Windows from not allowing to delete unlocked files, i.e., it will keep on
  // deleting other files even if it reaches a locked file first.
  protected static void deleteFilesRecursively(File folder) {
    TaskUtil.checkInterruption();

    // First check if it's a directory to avoid future misuse.
    if (folder.isDirectory()) {
      File[] files = folder.listFiles();
      for (File file : files) {
        TaskUtil.checkInterruption();

        if (file.isDirectory()) {
          deleteFilesRecursively(file);
        }
        else {
          try {
            FileUtils.forceDelete(file);
          }
          catch (IOException ioe) {
            ioe.printStackTrace();
          }
        }
      }
      // After cleaning the files, tries to delete the containing folder.
      try {
        FileUtils.forceDelete(folder);
      }
      catch (IOException ioe) {
        // If the folder cannot be deleted it means there are locked files in it. But we don't need to log it
        // here once the file locks had already been detected and logged in the for loop above.
      }
    }
    else {
      try {
        FileUtils.forceDelete(folder);
      }
      catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }
}
