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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.sonatype.nexus.util.WrappingInputStream;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A large-capacity spool which streams its bytes to and from a temporary file.
 *
 * @since 3.0
 */
public class TempFileSpool
    implements Spool
{
  private static final Logger logger = LoggerFactory.getLogger(TempFileSpool.class);

  private static final int BUFFER_SIZE = 1 * 1024 * 1024;

  private Path spoolFile;

  @Override
  public OutputStream createOutputStream() throws IOException {
    Preconditions.checkState(spoolFile == null, "The spool output stream has already been created.");

    spoolFile = Files.createTempFile("stream", "spool");
    logger.debug("Spooling content to {}", spoolFile);

    return new BufferedOutputStream(Files.newOutputStream(spoolFile, StandardOpenOption.APPEND), BUFFER_SIZE);
  }

  @Override
  public InputStream createInputStream() throws IOException {
    return new DeleteOnCloseInputStream(Files.newInputStream(spoolFile), spoolFile);
  }

  class DeleteOnCloseInputStream
      extends WrappingInputStream
  {
    DeleteOnCloseInputStream(final InputStream wrappedStream, final Path fileToDelete) {
      super(wrappedStream);
    }

    @Override
    public void close() throws IOException {
      try {
        super.close();
      }
      finally {
        logger.debug("Deleting spool file {}", spoolFile);
        Files.deleteIfExists(spoolFile);
      }
    }
  }
}
