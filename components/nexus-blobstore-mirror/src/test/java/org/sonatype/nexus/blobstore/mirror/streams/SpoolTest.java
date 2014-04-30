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
import java.util.Random;

import com.google.common.io.ByteStreams;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @since 3.0
 */
public class SpoolTest
{
  private byte[] bytes;

  @Before
  public void setUp(){
    bytes = new byte[1 * 1024 * 1024]; // one meg
    new Random().nextBytes(bytes);
  }

  @Test
  public void fileSpoolRoundTripTest() throws IOException {
    testRoundTrip(new TempFileSpool(), bytes);
  }

  @Test
  public void byteArraySpoolRoundTripTest() throws IOException {
    testRoundTrip(new ByteArraySpool(), bytes);
  }

  private void testRoundTrip(final Spool spool, final byte[] bytes) throws IOException {
    try (
        final OutputStream outputStream = spool.createOutputStream()) {
      ByteStreams.copy(new ByteArrayInputStream(bytes), outputStream);
    }

    final ByteArrayOutputStream to = new ByteArrayOutputStream();
    try (
        final InputStream inputStream = spool.createInputStream()) {
      ByteStreams.copy(inputStream, to);
    }


    final byte[] roundTripBytes = to.toByteArray();
    assertThat(roundTripBytes.length, equalTo(bytes.length));
    assertThat("bytes", roundTripBytes, equalTo(bytes));
  }
}
