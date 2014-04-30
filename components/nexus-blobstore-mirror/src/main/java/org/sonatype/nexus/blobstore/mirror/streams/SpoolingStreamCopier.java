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
package org.sonatype.nexus.blobstore.mirror.streams;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.blobstore.mirror.streams.Spool;
import org.sonatype.nexus.blobstore.mirror.streams.StreamCopier;
import org.sonatype.nexus.util.WrappingInputStream;

import com.google.common.base.Preconditions;
import org.apache.commons.io.input.TeeInputStream;

/**
 * Copies bytes as they are read from an input stream, spooling them through a temporary file to prevent memory filling
 * up in the case of very large blobs.
 */
public class SpoolingStreamCopier
    implements StreamCopier
{
  private CloseAwareInputStream splitter;

  private Spool spool;

  public SpoolingStreamCopier(InputStream inputStream, Spool spool) throws IOException {
    splitter = new CloseAwareInputStream(
        new TeeInputStream(inputStream, spool.createOutputStream()));

    this.spool = spool;
  }

  @Override
  public InputStream getOriginalStream() {
    return splitter;
  }

  @Override
  public InputStream getCopiedStream() throws IOException {
    Preconditions.checkState(!splitter.isOpen(), "Close the original stream first.");
    return spool.createInputStream();
  }

  static class CloseAwareInputStream
      extends WrappingInputStream
  {
    public CloseAwareInputStream(final InputStream wrappedStream) {
      super(wrappedStream);
    }

    private boolean open = true;

    @Override
    public void close() throws IOException {
      super.close();
      open = false;
    }

    public boolean isOpen() {
      return open;
    }
  }
}
