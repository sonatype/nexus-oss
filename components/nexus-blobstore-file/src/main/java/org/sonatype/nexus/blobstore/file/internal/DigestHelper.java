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

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.sonatype.nexus.orient.Hex;

import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Digest helper.
 *
 * @since 3.0
 */
public class DigestHelper
{
  private static byte[] calculateDigest(final MessageDigest digest, final InputStream input) throws IOException {
    byte[] buffer = new byte[1024];
    int n;
    do {
      n = input.read(buffer);
      if (n > 0) {
        digest.update(buffer, 0, n);
      }
    }
    while (n != -1);

    return digest.digest();
  }

  public static MessageDigest createSha1() {
    try {
      return MessageDigest.getInstance("SHA1");
    }
    catch (NoSuchAlgorithmException e) {
      // should never happen
      throw Throwables.propagate(e);
    }
  }

  /**
   * Calculates a HEX-encoded SHA1 digest for a stream.
   */
  public static String calculateSha1(final InputStream input) {
    checkNotNull(input);
    try {
      byte[] digest = calculateDigest(createSha1(), input);
      return Hex.encode(digest);
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
