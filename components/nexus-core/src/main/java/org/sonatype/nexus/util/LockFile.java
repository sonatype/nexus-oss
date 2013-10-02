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
import java.lang.management.ManagementFactory;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.Charset;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * File locker interface, inspired by Eclipse Locker.
 * 
 * @since 2.7.0
 */
public class LockFile
{
  private static final byte[] DEFAULT_PAYLOAD = ManagementFactory.getRuntimeMXBean().getName().getBytes(
      Charset.forName("UTF-8"));

  private final File lockFile;
  private FileLock fileLock;
  private RandomAccessFile randomAccessFile;
  private byte[] payload;

  public LockFile(final File lockFile) {
    this(lockFile, DEFAULT_PAYLOAD);
  }

  public LockFile(final File lockFile, final byte[] payload) {
    this.lockFile = checkNotNull(lockFile);
    this.payload = checkNotNull(payload);
  }

  public File getFile() {
    return lockFile;
  }

  public byte[] getPayload() {
    return payload;
  }

  public synchronized boolean lock() {
    if (fileLock != null) {
      return true;
    }
    try {
      randomAccessFile = new RandomAccessFile(lockFile, "rws");
      fileLock = randomAccessFile.getChannel().tryLock(0L, 1L, false);
      if (fileLock != null) {
        randomAccessFile.setLength(0);
        randomAccessFile.seek(0);
        randomAccessFile.write(payload);
      }
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
        release();
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
