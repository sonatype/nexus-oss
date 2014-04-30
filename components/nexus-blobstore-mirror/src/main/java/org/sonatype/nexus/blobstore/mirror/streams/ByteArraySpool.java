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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.base.Preconditions;

/**
 * A simple in-memory Spool. Probably the best thing for blobs that are less than a few megabytes.
 *
 * @since 3.0
 */
public class ByteArraySpool
    implements Spool
{
  private ByteArrayOutputStream byteArrayOutputStream;

  @Override
  public OutputStream createOutputStream() throws IOException {
    Preconditions.checkState(byteArrayOutputStream == null, "The output stream has already been created.");
    return this.byteArrayOutputStream = new ByteArrayOutputStream();
  }

  @Override
  public InputStream createInputStream() throws IOException {
    Preconditions.checkState(byteArrayOutputStream != null, "The output stream has not yet been created.");
    return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
  }
}
