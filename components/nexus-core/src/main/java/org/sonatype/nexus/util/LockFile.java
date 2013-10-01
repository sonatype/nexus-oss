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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * File locker interface, inspired by Eclipse Locker.
 * 
 * @since 2.7.0
 */
public class LockFile
{
  private final File lockFile;
  private FileLock fileLock;
  private RandomAccessFile randomAccessFile;

  public LockFile(final File lockFile) {
    this.lockFile = checkNotNull(lockFile);
  }
  
  public File getFile() {
    return lockFile;
  }

  public synchronized boolean lock() {
    try {
      randomAccessFile = new RandomAccessFile(lockFile, "rw");
      fileLock = randomAccessFile.getChannel().tryLock(0L, 1L, false);
    }
    catch (IOException e) {
      // handle it as null result
      fileLock = null;
    }
    catch (OverlappingFileLockException e) {
      // handle it as null result
      fileLock = null;
    }
    finally {
      if (fileLock == null) {
        close(randomAccessFile);
        randomAccessFile = null;
        return false;
      }
    }
    return true;
  }

  public synchronized void release() {
    close(fileLock);
    fileLock = null;
    close(randomAccessFile);
    randomAccessFile = null;
  }

  private void close(AutoCloseable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      }
      catch (Exception e) {
        // muted
      }
    }
  }
}
