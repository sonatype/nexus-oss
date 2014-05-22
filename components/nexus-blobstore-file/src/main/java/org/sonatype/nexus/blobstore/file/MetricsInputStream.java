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
package org.sonatype.nexus.blobstore.file;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.sonatype.nexus.blobstore.file.utils.DigesterUtils;

import com.google.common.io.CountingInputStream;

/**
 * A utility to collect metrics about the content of an input stream.
 *
 * @since 3.0
 */
public class MetricsInputStream
    extends FilterInputStream
{
  private MessageDigest messageDigest;

  private CountingInputStream countingInputStream;

  public static MetricsInputStream metricsInputStream(final InputStream wrappedStream, final String algorithm)
      throws NoSuchAlgorithmException
  {
    final MessageDigest digest = MessageDigest.getInstance(algorithm);
    return new MetricsInputStream(new CountingInputStream(wrappedStream), digest);
  }

  private MetricsInputStream(final CountingInputStream countingStream, final MessageDigest messageDigest) {
    super(new DigestInputStream(countingStream, messageDigest));
    this.messageDigest = messageDigest;
    this.countingInputStream = countingStream;
  }

  public String getMessageDigest() {
    return DigesterUtils.getDigestAsString(messageDigest.digest());
  }

  public long getSize() {
    return countingInputStream.getCount();
  }
}
