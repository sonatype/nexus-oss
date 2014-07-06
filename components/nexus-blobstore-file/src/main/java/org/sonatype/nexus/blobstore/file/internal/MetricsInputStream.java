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
package org.sonatype.nexus.blobstore.file.internal;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.sonatype.nexus.blobstore.file.FileOperations.StreamMetrics;
import org.sonatype.nexus.orient.Hex;

import com.google.common.io.CountingInputStream;

/**
 * A utility to collect metrics about the content of an input stream.
 *
 * @since 3.0
 */
public class MetricsInputStream
    extends FilterInputStream
{
  private final MessageDigest messageDigest;

  private final CountingInputStream countingInputStream;

  public MetricsInputStream(final InputStream input, final MessageDigest messageDigest) {
    this(new CountingInputStream(input), messageDigest);
  }

  private MetricsInputStream(final CountingInputStream countingStream, final MessageDigest messageDigest) {
    super(new DigestInputStream(countingStream, messageDigest));
    this.messageDigest = messageDigest;
    this.countingInputStream = countingStream;
  }

  public String getMessageDigest() {
    return Hex.encode(messageDigest.digest());
  }

  public long getSize() {
    return countingInputStream.getCount();
  }

  public StreamMetrics getMetrics() {
    return new StreamMetrics(getSize(), getMessageDigest());
  }
}
