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

import java.io.InputStream;
import java.security.MessageDigest;

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
  private static String getDigest(final String algorithm, final InputStream input) throws Exception {
    MessageDigest md = MessageDigest.getInstance(algorithm);

    byte[] buffer = new byte[1024];
    int n;
    do {
      n = input.read(buffer);
      if (n > 0) {
        md.update(buffer, 0, n);
      }
    }
    while (n != -1);

    return Hex.encode(md.digest());
  }

  /**
   * Calculates a SHA1 digest for a stream.
   */
  public static String getSha1Digest(final InputStream input) {
    checkNotNull(input);
    try {
      return getDigest("SHA1", input);
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
