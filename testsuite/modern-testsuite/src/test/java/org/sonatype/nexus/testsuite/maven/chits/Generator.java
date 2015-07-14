/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.maven.chits;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * Content generator.
 */
public abstract class Generator
    extends ComponentSupport
{
  protected static InputStream exactLength(final byte[] sample, final long length) {
    return new InputStream()
    {
      private long pos = 0;

      public int read() throws IOException {
        return pos < length ?
            sample[(int) (pos++ % sample.length)] :
            -1;
      }
    };
  }

  protected static InputStream repeat(final byte[] sample, final long times) {
    return new InputStream()
    {
      private long pos = 0;

      private final long total = (long) sample.length * times;

      public int read() throws IOException {
        return pos < total ?
            sample[(int) (pos++ % sample.length)] :
            -1;
      }
    };
  }

  /**
   * Returns the content type it generates.
   */
  public abstract String getContentType();

  /**
   * Returns the exact byte numbers generated for requested length. Some formats will return the passed in value, but
   * some, those "record based" for example will return a modified one (aligned with record or segment boundaries).
   */
  public abstract long getExactContentLength(long length);

  /**
   * Generates content of given length.
   */
  public abstract InputStream generate(long length);
}
