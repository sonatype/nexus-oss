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
package org.sonatype.nexus.blobstore.inmemory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobMetrics;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

/**
 * A prototype to experiment with usage scenarios.  This and the in-memory blob store will probably go away soon.
 *
 * @since 3.0
 */
public class InMemoryBlobStoreTest extends TestSupport
{
  private BlobStore blobStore;

  @Before
  public void setUp() {
    blobStore = new InMemoryBlobStore();
  }

  @Test
  public void walkThroughUsage() throws Exception {

    final String content = "Hello";
    final InputStream inputStream = toInputStream(content);

    final Map<String, String> headers = ImmutableMap.of("name", "/sauce/chocolate/stale");
    final Blob blob = blobStore.create(inputStream, headers);
    Blob retrievedBlob = blobStore.get(blob.getId());

    String result = deserialize(retrievedBlob.getInputStream());
    assertThat(result, equalTo(content));
  }

  @Test
  public void testBlobMetrics() throws Exception {
    final String content = "My Content";
    final InputStream inputStream = toInputStream(content);

    final Blob blob = blobStore.create(inputStream, ImmutableMap.of("name", "/robots/neurotic/marvin"));

    final BlobMetrics metrics = blob.getMetrics();

    assertThat(metrics.getTotalSize(), greaterThan(metrics.getContentSize()));
    assertThat(metrics.getTotalSize(), greaterThan(metrics.getHeaderSize()));
  }

  private InputStream toInputStream(final Object object)
      throws IOException
  {
    return new ByteArrayInputStream(serialize(object));
  }

  private byte[] serialize(final Object object) throws IOException {
    ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
    try (ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput)) {
      objectOutput.writeObject(object);
    }
    return byteOutput.toByteArray();
  }

  private <O> O deserialize(final InputStream inputStream) throws IOException, ClassNotFoundException {
    try (ObjectInputStream o = new ObjectInputStream(inputStream)) {
      return (O) o.readObject();
    }
  }
}