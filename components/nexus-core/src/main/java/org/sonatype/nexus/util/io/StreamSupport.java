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

package org.sonatype.nexus.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.nexus.util.file.FileSupport;

import com.google.common.io.CharStreams;

/**
 * Stream related support class. Offers static helper methods for common stream related operations
 * used in Nexus core and plugins for manipulating streams.
 *
 * @author cstamas
 * @since 2.7.0
 */
public final class StreamSupport
{
  private StreamSupport() {
    // no instance
  }

  private static final int BUFFER_SIZE = SystemPropertiesHelper.getInteger(StreamSupport.class.getName()
      + ".BUFFER_SIZE", 4096);

  /**
   * There is no "magic" buffer size the fits all.
   *
   * @deprecated Use the {@link #copy(InputStream, OutputStream, int)} method instead.
   */
  @Deprecated
  public static long copy(final InputStream from, final OutputStream to) throws IOException {
    return copy(from, to, BUFFER_SIZE);
  }

  public static long copy(final InputStream from, final OutputStream to, final int bufferSize) throws IOException {
    final byte[] buf = new byte[bufferSize];
    long count = 0;
    while (true) {
      int r = from.read(buf);
      if (r == -1) {
        break;
      }
      count += r;
      to.write(buf, 0, r);
    }
    return count;
  }

  public static String asString(final InputStream is) throws IOException {
    return asString(is, FileSupport.DEFAULT_CHARSET);
  }

  public static String asString(final InputStream is, final Charset cs) throws IOException {
    try (final InputStreamReader isr = new InputStreamReader(is, cs)) {
      return CharStreams.toString(isr);
    }
  }

}
