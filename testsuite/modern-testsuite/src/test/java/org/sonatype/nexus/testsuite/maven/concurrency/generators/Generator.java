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
package org.sonatype.nexus.testsuite.maven.concurrency.generators;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.sonatype.sisu.goodies.common.ByteSize;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

/**
 * Content generator.
 */
public abstract class Generator
    extends ComponentSupport
{
  /**
   * HttpClient entity that is backed by a {@link Generator}.
   */
  public static HttpEntity generatedEntity(final Generator generator, final ByteSize length) {
    final long exactContentLength = generator.getExactContentLength(length.toBytes());
    final Header contentType = new BasicHeader(HttpHeaders.CONTENT_TYPE, generator.getContentType());
    return new HttpEntity()
    {
      @Override
      public boolean isRepeatable() {
        return true;
      }

      @Override
      public boolean isChunked() {
        return false;
      }

      @Override
      public long getContentLength() {
        return exactContentLength;
      }

      @Override
      public Header getContentType() {
        return contentType;
      }

      @Override
      public Header getContentEncoding() {
        return null;
      }

      @Override
      public InputStream getContent() throws IOException, UnsupportedOperationException {
        return generator.generate(exactContentLength);
      }

      @Override
      public void writeTo(final OutputStream outstream) throws IOException {
        ByteStreams.copy(getContent(), outstream);
      }

      @Override
      public boolean isStreaming() {
        return false;
      }

      @Override
      public void consumeContent() throws IOException {
        // nop
      }
    };
  }

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
