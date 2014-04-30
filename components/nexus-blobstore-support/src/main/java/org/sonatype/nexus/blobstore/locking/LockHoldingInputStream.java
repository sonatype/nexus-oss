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
package org.sonatype.nexus.blobstore.locking;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.util.WrappingInputStream;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An input stream decorator that holds a BlobLock until such time that the stream is closed.
 *
 * @since 3.0
 */
public class LockHoldingInputStream
    extends WrappingInputStream
{
  private static final Logger logger = LoggerFactory.getLogger(LockHoldingInputStream.class);

  private BlobLock lock;

  public LockHoldingInputStream(final InputStream wrappedStream, final BlobLock lock) {
    super(wrappedStream);
    Preconditions.checkNotNull(wrappedStream);
    Preconditions.checkNotNull(lock);
    this.lock = lock;
  }

  @Override
  public void close() throws IOException {
    try {
      super.close();
    }
    finally {
      lock.close();
      lock = null;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      if (lock != null) {
        lock.close();
        logger.warn("Blob lock leakage detected: {}", lock);
      }
    }
    finally {
      super.finalize();
    }
  }

}
